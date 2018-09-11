package org.openstreetmap.josm.plugins.tofix.util;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.openstreetmap.josm.data.Version;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.Compression;
import org.openstreetmap.josm.io.NetworkManager;
import org.openstreetmap.josm.io.ProgressInputStream;
import org.openstreetmap.josm.io.ProgressOutputStream;
import org.openstreetmap.josm.io.UTFInputStreamReader;
import org.openstreetmap.josm.io.auth.DefaultAuthenticator;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Utils;

public final class HttpClient {

    private URL url;
    private final String requestMethod;
    private int connectTimeout = (int) TimeUnit.SECONDS.toMillis(15);
    private int readTimeout = (int) TimeUnit.SECONDS.toMillis(30);
    private byte[] requestBody;
    private long ifModifiedSince;
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private int maxRedirects = 5;
    private boolean useCache;
    private String reasonForRequest;
    private String outputMessage = tr("Uploading data ...");
    private HttpURLConnection connection; // to allow disconnecting before `response` is set
    private Response response;
    private boolean finishOnCloseOutput = true;

    private static final Pattern TOMCAT_ERR_MESSAGE = Pattern.compile(
        ".*<p><b>[^<]+</b>[^<]+</p><p><b>[^<]+</b> (?:<u>)?([^<]*)(?:</u>)?</p><p><b>[^<]+</b> (?:<u>)?[^<]*(?:</u>)?</p>.*",
        Pattern.CASE_INSENSITIVE);

    static {
        CookieHandler.setDefault(new CookieManager());
    }

    private HttpClient(URL url, String requestMethod) {
        this.url = url;
        this.requestMethod = requestMethod;
        this.headers.put("Accept-Encoding", "gzip");
    }

    public Response connect() throws IOException {
        return connect(null);
    }

    public Response connect(ProgressMonitor progressMonitor) throws IOException {
        if (progressMonitor == null) {
            progressMonitor = NullProgressMonitor.INSTANCE;
        }
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        this.connection = connection;
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("User-Agent", Version.getInstance().getFullAgentString());
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setInstanceFollowRedirects(false); // we do that ourselves
        if (ifModifiedSince > 0) {
            connection.setIfModifiedSince(ifModifiedSince);
        }
        connection.setUseCaches(useCache);
        if (!useCache) {
            connection.setRequestProperty("Cache-Control", "no-cache");
        }
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getValue() != null) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        progressMonitor.beginTask(tr("Contacting Server..."), 1);
        progressMonitor.indeterminateSubTask(null);

        if ("PUT".equals(requestMethod) || "POST".equals(requestMethod) || "DELETE".equals(requestMethod)) {
            Logging.info("{0} {1} ({2}) ...", requestMethod, url, Utils.getSizeString(requestBody.length, Locale.getDefault()));
            if (Logging.isTraceEnabled() && requestBody.length > 0) {
                Logging.trace("BODY: {0}", new String(requestBody, StandardCharsets.UTF_8));
            }
            connection.setFixedLengthStreamingMode(requestBody.length);
            connection.setDoOutput(true);
            try (OutputStream out = new BufferedOutputStream(
                    new ProgressOutputStream(connection.getOutputStream(), requestBody.length,
                            progressMonitor, outputMessage, finishOnCloseOutput))) {
                out.write(requestBody);
            }
        }

        boolean successfulConnection = false;
        try {
            try {
                connection.connect();
                final boolean hasReason = reasonForRequest != null && !reasonForRequest.isEmpty();
                Logging.info("{0} {1}{2} -> {3}{4}",
                        requestMethod, url, hasReason ? (" (" + reasonForRequest + ')') : "",
                        connection.getResponseCode(),
                        connection.getContentLengthLong() > 0
                                ? (" (" + Utils.getSizeString(connection.getContentLengthLong(), Locale.getDefault()) + ')')
                                : ""
                );
                if (Logging.isDebugEnabled()) {
                    Logging.debug("RESPONSE: {0}", connection.getHeaderFields());
                }
                if (DefaultAuthenticator.getInstance().isEnabled() && connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    DefaultAuthenticator.getInstance().addFailedCredentialHost(url.getHost());
                }
            } catch (IOException | IllegalArgumentException | NoSuchElementException e) {
                Logging.info("{0} {1} -> !!!", requestMethod, url);
                Logging.warn(e);
                //noinspection ThrowableResultOfMethodCallIgnored
                NetworkManager.addNetworkError(url, Utils.getRootCause(e));
                throw e;
            }
            if (isRedirect(connection.getResponseCode())) {
                final String redirectLocation = connection.getHeaderField("Location");
                if (redirectLocation == null) {
                    /* I18n: argument is HTTP response code */
                    throw new IOException(tr("Unexpected response from HTTP server. Got {0} response without ''Location'' header." +
                            " Can''t redirect. Aborting.", connection.getResponseCode()));
                } else if (maxRedirects > 0) {
                    url = new URL(url, redirectLocation);
                    maxRedirects--;
                    Logging.info(tr("Download redirected to ''{0}''", redirectLocation));
                    return connect();
                } else if (maxRedirects == 0) {
                    String msg = tr("Too many redirects to the download URL detected. Aborting.");
                    throw new IOException(msg);
                }
            }
            response = new Response(connection, progressMonitor);
            successfulConnection = true;
            return response;
        } finally {
            if (!successfulConnection) {
                connection.disconnect();
            }
        }
    }

    public Response getResponse() {
        return response;
    }

    public static final class Response {
        private final HttpURLConnection connection;
        private final ProgressMonitor monitor;
        private final int responseCode;
        private final String responseMessage;
        private boolean uncompress;
        private boolean uncompressAccordingToContentDisposition;
        private String responseData;

        private Response(HttpURLConnection connection, ProgressMonitor monitor) throws IOException {
            CheckParameterUtil.ensureParameterNotNull(connection, "connection");
            CheckParameterUtil.ensureParameterNotNull(monitor, "monitor");
            this.connection = connection;
            this.monitor = monitor;
            this.responseCode = connection.getResponseCode();
            this.responseMessage = connection.getResponseMessage();
            if (this.responseCode >= 300) {
                String contentType = getContentType();
                if (contentType == null || (
                        contentType.contains("text") ||
                        contentType.contains("html") ||
                        contentType.contains("xml"))
                        ) {
                    String content = this.fetchContent();
                    if (content.isEmpty()) {
                        Logging.debug("Server did not return any body");
                    } else {
                        Logging.debug("Response body: ");
                        Logging.debug(this.fetchContent());
                    }
                } else {
                    Logging.debug("Server returned content: {0} of length: {1}. Not printing.", contentType, this.getContentLength());
                }
            }
        }

        public Response uncompress(boolean uncompress) {
            this.uncompress = uncompress;
            return this;
        }

        public Response uncompressAccordingToContentDisposition(boolean uncompressAccordingToContentDisposition) {
            this.uncompressAccordingToContentDisposition = uncompressAccordingToContentDisposition;
            return this;
        }

        public URL getURL() {
            return connection.getURL();
        }

        public String getRequestMethod() {
            return connection.getRequestMethod();
        }

        @SuppressWarnings("resource")
        public InputStream getContent() throws IOException {
            InputStream in;
            try {
                in = connection.getInputStream();
            } catch (IOException ioe) {
                Logging.debug(ioe);
                in = Optional.ofNullable(connection.getErrorStream()).orElseGet(() -> new ByteArrayInputStream(new byte[]{}));
            }
            in = new ProgressInputStream(in, getContentLength(), monitor);
            in = "gzip".equalsIgnoreCase(getContentEncoding()) ? new GZIPInputStream(in) : in;
            Compression compression = Compression.NONE;
            if (uncompress) {
                final String contentType = getContentType();
                Logging.debug("Uncompressing input stream according to Content-Type header: {0}", contentType);
                compression = Compression.forContentType(contentType);
            }
            if (uncompressAccordingToContentDisposition && Compression.NONE.equals(compression)) {
                final String contentDisposition = getHeaderField("Content-Disposition");
                final Matcher matcher = Pattern.compile("filename=\"([^\"]+)\"").matcher(
                        contentDisposition != null ? contentDisposition : "");
                if (matcher.find()) {
                    Logging.debug("Uncompressing input stream according to Content-Disposition header: {0}", contentDisposition);
                    compression = Compression.byExtension(matcher.group(1));
                }
            }
            in = compression.getUncompressedInputStream(in);
            return in;
        }

        public BufferedReader getContentReader() throws IOException {
            return new BufferedReader(
                    UTFInputStreamReader.create(getContent())
            );
        }

        public synchronized String fetchContent() throws IOException {
            if (responseData == null) {
                try (Scanner scanner = new Scanner(getContentReader()).useDelimiter("\\A")) { // \A - beginning of input
                    responseData = scanner.hasNext() ? scanner.next() : "";
                }
            }
            return responseData;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getResponseMessage() {
            return responseMessage;
        }

        public String getContentEncoding() {
            return connection.getContentEncoding();
        }

        public String getContentType() {
            return connection.getHeaderField("Content-Type");
        }

        public long getExpiration() {
            return connection.getExpiration();
        }

        public long getLastModified() {
            return connection.getLastModified();
        }

        public long getContentLength() {
            return connection.getContentLengthLong();
        }

        public String getHeaderField(String name) {
            return connection.getHeaderField(name);
        }

        public Map<String, List<String>> getHeaderFields() {
            Map<String, List<String>> ret = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (Entry<String, List<String>> e: connection.getHeaderFields().entrySet()) {
                if (e.getKey() != null) {
                    ret.put(e.getKey(), e.getValue());
                }
            }
            return Collections.unmodifiableMap(ret);
        }

        public void disconnect() {
            HttpClient.disconnect(connection);
        }
    }

    public static HttpClient create(URL url) {
        return create(url, "GET");
    }

    public static HttpClient create(URL url, String requestMethod) {
        return new HttpClient(url, requestMethod);
    }

    public URL getURL() {
        return url;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestHeader(String header) {
        return headers.get(header);
    }

    public HttpClient useCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    public HttpClient keepAlive(boolean keepAlive) {
        return setHeader("Connection", keepAlive ? null : "close");
    }

    public HttpClient setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public HttpClient setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpClient setAccept(String accept) {
        return setHeader("Accept", accept);
    }

    public HttpClient setRequestBody(byte[] requestBody) {
        this.requestBody = Utils.copyArray(requestBody);
        return this;
    }

    public HttpClient setIfModifiedSince(long ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
        return this;
    }

    public HttpClient setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
        return this;
    }

    public HttpClient setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpClient setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public HttpClient setReasonForRequest(String reasonForRequest) {
        this.reasonForRequest = reasonForRequest;
        return this;
    }

    public HttpClient setOutputMessage(String outputMessage) {
        this.outputMessage = outputMessage;
        return this;
    }

    public HttpClient setFinishOnCloseOutput(boolean finishOnCloseOutput) {
        this.finishOnCloseOutput = finishOnCloseOutput;
        return this;
    }

    private static boolean isRedirect(final int statusCode) {
        switch (statusCode) {
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
            case HttpURLConnection.HTTP_SEE_OTHER:
            case 307: // TEMPORARY_REDIRECT:
            case 308: // PERMANENT_REDIRECT:
                return true;
            default:
                return false;
        }
    }

    public void disconnect() {
        HttpClient.disconnect(connection);
    }

    private static void disconnect(final HttpURLConnection connection) {
        if (connection != null) {
            connection.setConnectTimeout(100);
            connection.setReadTimeout(100);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logging.warn("InterruptedException in " + HttpClient.class + " during cancel");
                Thread.currentThread().interrupt();
            }
            connection.disconnect();
        }
    }

    public static Matcher getTomcatErrorMatcher(String data) {
        return data != null ? TOMCAT_ERR_MESSAGE.matcher(data) : null;
    }
}
