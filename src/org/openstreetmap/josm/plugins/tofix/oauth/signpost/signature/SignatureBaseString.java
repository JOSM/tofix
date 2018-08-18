package org.openstreetmap.josm.plugins.tofix.oauth.signpost.signature;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.openstreetmap.josm.plugins.tofix.oauth.signpost.OAuth;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthMessageSignerException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpParameters;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpRequest;

public class SignatureBaseString {

    private HttpRequest request;

    private HttpParameters requestParameters;

    public SignatureBaseString(HttpRequest request, HttpParameters requestParameters) {
        this.request = request;
        this.requestParameters = requestParameters;
    }

    public String generate() throws OAuthMessageSignerException {

        try {
            String normalizedUrl = normalizeRequestUrl();
            String normalizedParams = normalizeRequestParameters();

            return request.getMethod() + '&' + OAuth.percentEncode(normalizedUrl) + '&'
                    + OAuth.percentEncode(normalizedParams);
        } catch (Exception e) {
            throw new OAuthMessageSignerException(e);
        }
    }

    public String normalizeRequestUrl() throws URISyntaxException {
        URI uri = new URI(request.getRequestUrl());
        String scheme = uri.getScheme().toLowerCase();
        String authority = uri.getAuthority().toLowerCase();
        boolean dropPort = (scheme.equals("http") && uri.getPort() == 80)
                || (scheme.equals("https") && uri.getPort() == 443);
        if (dropPort) {
            // find the last : in the authority
            int index = authority.lastIndexOf(":");
            if (index >= 0) {
                authority = authority.substring(0, index);
            }
        }
        String path = uri.getRawPath();
        if (path == null || path.length() <= 0) {
            path = "/"; // conforms to RFC 2616 section 3.2.2
        }
        // we know that there is no query and no fragment here.
        return scheme + "://" + authority + path;
    }

    public String normalizeRequestParameters() throws IOException {
        if (requestParameters == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = requestParameters.keySet().iterator();

        for (int i = 0; iter.hasNext(); i++) {
            String param = iter.next();

            if (OAuth.OAUTH_SIGNATURE.equals(param) || "realm".equals(param)) {
                continue;
            }

            if (i > 0) {
                sb.append("&");
            }

            // fix contributed by Stjepan Rajko
            // since param should already be encoded, we supply false for percentEncode
            sb.append(requestParameters.getAsQueryString(param, false));  
        }
        return sb.toString();
    }
}
