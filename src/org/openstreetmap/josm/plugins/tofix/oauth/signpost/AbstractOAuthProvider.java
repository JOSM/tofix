package org.openstreetmap.josm.plugins.tofix.oauth.signpost;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthCommunicationException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthExpectationFailedException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthMessageSignerException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthNotAuthorizedException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpParameters;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpRequest;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpResponse;

/**
 * ABC for all provider implementations. If you're writing a custom provider,
 * you will probably inherit from this class, since it takes a lot of work from
 * you.
 *
 * @author Matthias Kaeppler
 */
public abstract class AbstractOAuthProvider implements OAuthProvider {

    private static final long serialVersionUID = 1L;

    private String requestTokenEndpointUrl;

    private String accessTokenEndpointUrl;

    private String authorizationWebsiteUrl;

    private HttpParameters responseParameters;

    private Map<String, String> defaultHeaders;

    private boolean isOAuth10a;

    private transient OAuthProviderListener listener;

    public AbstractOAuthProvider(String requestTokenEndpointUrl, String accessTokenEndpointUrl,
            String authorizationWebsiteUrl) {
        this.requestTokenEndpointUrl = requestTokenEndpointUrl;
        this.accessTokenEndpointUrl = accessTokenEndpointUrl;
        this.authorizationWebsiteUrl = authorizationWebsiteUrl;
        this.responseParameters = new HttpParameters();
        this.defaultHeaders = new HashMap<>();
    }

    @Override
    public synchronized String retrieveRequestToken(OAuthConsumer consumer, String callbackUrl,
            String... customOAuthParams) throws OAuthMessageSignerException,
            OAuthNotAuthorizedException, OAuthExpectationFailedException,
            OAuthCommunicationException {

        // invalidate current credentials, if any
        consumer.setTokenWithSecret(null, null);

        // 1.0a expects the callback to be sent while getting the request token.
        // 1.0 service providers would simply ignore this parameter.
        HttpParameters params = new HttpParameters();
        params.putAll(customOAuthParams, true);
        params.put(OAuth.OAUTH_CALLBACK, callbackUrl, true);

        retrieveToken(consumer, requestTokenEndpointUrl, params);

        String callbackConfirmed = responseParameters.getFirst(OAuth.OAUTH_CALLBACK_CONFIRMED);
        responseParameters.remove(OAuth.OAUTH_CALLBACK_CONFIRMED);
        isOAuth10a = Boolean.TRUE.toString().equals(callbackConfirmed);

        // 1.0 service providers expect the callback as part of the auth URL,
        // Do not send when 1.0a.
        if (isOAuth10a) {
            return OAuth.addQueryParameters(authorizationWebsiteUrl, OAuth.OAUTH_TOKEN,
                consumer.getToken());
        } else {
            return OAuth.addQueryParameters(authorizationWebsiteUrl, OAuth.OAUTH_TOKEN,
                consumer.getToken(), OAuth.OAUTH_CALLBACK, callbackUrl);
        }
    }

    @Override
    public synchronized void retrieveAccessToken(OAuthConsumer consumer, String oauthVerifier,
            String... customOAuthParams) throws OAuthMessageSignerException,
            OAuthNotAuthorizedException, OAuthExpectationFailedException,
            OAuthCommunicationException {

        if (consumer.getToken() == null || consumer.getTokenSecret() == null) {
            throw new OAuthExpectationFailedException(
                    "Authorized request token or token secret not set. "
                            + "Did you retrieve an authorized request token before?");
        }

        HttpParameters params = new HttpParameters();
        params.putAll(customOAuthParams, true);

        if (isOAuth10a && oauthVerifier != null) {
            params.put(OAuth.OAUTH_VERIFIER, oauthVerifier, true);
        }
        retrieveToken(consumer, accessTokenEndpointUrl, params);
    }

    protected void retrieveToken(OAuthConsumer consumer, String endpointUrl,
            HttpParameters customOAuthParams) throws OAuthMessageSignerException,
            OAuthCommunicationException, OAuthNotAuthorizedException,
            OAuthExpectationFailedException {
        Map<String, String> defaultHeaders = getRequestHeaders();

        if (consumer.getConsumerKey() == null || consumer.getConsumerSecret() == null) {
            throw new OAuthExpectationFailedException("Consumer key or secret not set");
        }

        HttpRequest request = null;
        HttpResponse response = null;
        try {
            request = createRequest(endpointUrl);
            for (String header : defaultHeaders.keySet()) {
                request.setHeader(header, defaultHeaders.get(header));
            }
            if (customOAuthParams != null && !customOAuthParams.isEmpty()) {
                consumer.setAdditionalParameters(customOAuthParams);
            }

            if (this.listener != null) {
                this.listener.prepareRequest(request);
            }

            consumer.sign(request);

            if (this.listener != null) {
                this.listener.prepareSubmission(request);
            }

            response = sendRequest(request);
            int statusCode = response.getStatusCode();

            boolean requestHandled = false;
            if (this.listener != null) {
                requestHandled = this.listener.onResponseReceived(request, response);
            }
            if (requestHandled) {
                return;
            }

            if (statusCode >= 300) {
                handleUnexpectedResponse(statusCode, response);
            }

            HttpParameters responseParams = OAuth.decodeForm(response.getContent());

            String token = responseParams.getFirst(OAuth.OAUTH_TOKEN);
            String secret = responseParams.getFirst(OAuth.OAUTH_TOKEN_SECRET);
            responseParams.remove(OAuth.OAUTH_TOKEN);
            responseParams.remove(OAuth.OAUTH_TOKEN_SECRET);

            setResponseParameters(responseParams);

            if (token == null || secret == null) {
                throw new OAuthExpectationFailedException(
                        "Request token or token secret not set in server reply. "
                                + "The service provider you use is probably buggy.");
            }

            consumer.setTokenWithSecret(token, secret);

        } catch (OAuthNotAuthorizedException | OAuthExpectationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new OAuthCommunicationException(e);
        } finally {
            try {
                closeConnection(request, response);
            } catch (Exception e) {
                throw new OAuthCommunicationException(e);
            }
        }
    }

    protected void handleUnexpectedResponse(int statusCode, HttpResponse response) throws Exception {
        if (response == null) {
            return;
        }
        StringBuilder responseBody = new StringBuilder();
        InputStream content = response.getContent();
        if (content != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(content,StandardCharsets.UTF_8));

            String line = reader.readLine();
            while (line != null) {
                responseBody.append(line);
                line = reader.readLine();
            }
        }

        switch (statusCode) {
        case 401:
            throw new OAuthNotAuthorizedException(responseBody.toString());
        default:
            throw new OAuthCommunicationException("Service provider responded in error: "
                    + statusCode + " (" + response.getReasonPhrase() + ")", responseBody.toString());
        }
    }

    protected abstract HttpRequest createRequest(String endpointUrl) throws Exception;

    protected abstract HttpResponse sendRequest(HttpRequest request) throws Exception;

    protected void closeConnection(HttpRequest request, HttpResponse response) throws Exception {
        // NOP
    }

    @Override
    public HttpParameters getResponseParameters() {
        return responseParameters;
    }

    protected String getResponseParameter(String key) {
        return responseParameters.getFirst(key);
    }

    @Override
    public void setResponseParameters(HttpParameters parameters) {
        this.responseParameters = parameters;
    }

    @Override
    public void setOAuth10a(boolean isOAuth10aProvider) {
        this.isOAuth10a = isOAuth10aProvider;
    }

    @Override
    public boolean isOAuth10a() {
        return isOAuth10a;
    }

    @Override
    public String getRequestTokenEndpointUrl() {
        return this.requestTokenEndpointUrl;
    }

    @Override
    public String getAccessTokenEndpointUrl() {
        return this.accessTokenEndpointUrl;
    }

    @Override
    public String getAuthorizationWebsiteUrl() {
        return this.authorizationWebsiteUrl;
    }

    @Override
    public void setRequestHeader(String header, String value) {
        defaultHeaders.put(header, value);
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return defaultHeaders;
    }

    @Override
    public void setListener(OAuthProviderListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeListener(OAuthProviderListener listener) {
        this.listener = null;
    }
}
