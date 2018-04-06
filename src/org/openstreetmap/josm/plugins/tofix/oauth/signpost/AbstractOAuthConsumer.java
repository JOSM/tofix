package org.openstreetmap.josm.plugins.tofix.oauth.signpost;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openstreetmap.josm.plugins.tofix.oauth.signpost.basic.UrlStringRequestAdapter;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthCommunicationException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthExpectationFailedException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthMessageSignerException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpParameters;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpRequest;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.signature.AuthorizationHeaderSigningStrategy;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.signature.HmacSha1MessageSigner;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.signature.OAuthMessageSigner;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.signature.QueryStringSigningStrategy;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.signature.SigningStrategy;

public abstract class AbstractOAuthConsumer implements OAuthConsumer {

    private static final long serialVersionUID = 1L;

    private String consumerKey, consumerSecret;

    private String token;

    private OAuthMessageSigner messageSigner;

    private SigningStrategy signingStrategy;

    // these are params that may be passed to the consumer directly (i.e.
    // without going through the request object)
    private HttpParameters additionalParameters;

    // these are the params which will be passed to the message signer
    private HttpParameters requestParameters;

    private boolean sendEmptyTokens;

    final private Random random = new Random(System.nanoTime());

    public AbstractOAuthConsumer(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        setMessageSigner(new HmacSha1MessageSigner());
        setSigningStrategy(new AuthorizationHeaderSigningStrategy());
    }

    @Override
    public void setMessageSigner(OAuthMessageSigner messageSigner) {
        this.messageSigner = messageSigner;
        messageSigner.setConsumerSecret(consumerSecret);
    }

    @Override
    public void setSigningStrategy(SigningStrategy signingStrategy) {
        this.signingStrategy = signingStrategy;
    }

    @Override
    public void setAdditionalParameters(HttpParameters additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    @Override
    public synchronized HttpRequest sign(HttpRequest request) throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        if (consumerKey == null) {
            throw new OAuthExpectationFailedException("consumer key not set");
        }
        if (consumerSecret == null) {
            throw new OAuthExpectationFailedException("consumer secret not set");
        }

        requestParameters = new HttpParameters();
        try {
            if (additionalParameters != null) {
                requestParameters.putAll(additionalParameters, false);
            }
            collectHeaderParameters(request, requestParameters);
            collectQueryParameters(request, requestParameters);
            collectBodyParameters(request, requestParameters);

            // add any OAuth params that haven't already been set
            completeOAuthParameters(requestParameters);

            requestParameters.remove(OAuth.OAUTH_SIGNATURE);

        } catch (IOException e) {
            throw new OAuthCommunicationException(e);
        }

        String signature = messageSigner.sign(request, requestParameters);
        OAuth.debugOut("signature", signature);

        signingStrategy.writeSignature(signature, request, requestParameters);
        OAuth.debugOut("Request URL", request.getRequestUrl());

        return request;
    }

    @Override
    public synchronized HttpRequest sign(Object request) throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        return sign(wrap(request));
    }

    @Override
    public synchronized String sign(String url) throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException {
        HttpRequest request = new UrlStringRequestAdapter(url);

        // switch to URL signing
        SigningStrategy oldStrategy = this.signingStrategy;
        this.signingStrategy = new QueryStringSigningStrategy();

        sign(request);

        // revert to old strategy
        this.signingStrategy = oldStrategy;

        return request.getRequestUrl();
    }

    protected abstract HttpRequest wrap(Object request);

    @Override
    public void setTokenWithSecret(String token, String tokenSecret) {
        this.token = token;
        messageSigner.setTokenSecret(tokenSecret);
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getTokenSecret() {
        return messageSigner.getTokenSecret();
    }

    @Override
    public String getConsumerKey() {
        return this.consumerKey;
    }

    @Override
    public String getConsumerSecret() {
        return this.consumerSecret;
    }

    protected void completeOAuthParameters(HttpParameters out) {
        if (!out.containsKey(OAuth.OAUTH_CONSUMER_KEY)) {
            out.put(OAuth.OAUTH_CONSUMER_KEY, consumerKey, true);
        }
        if (!out.containsKey(OAuth.OAUTH_SIGNATURE_METHOD)) {
            out.put(OAuth.OAUTH_SIGNATURE_METHOD, messageSigner.getSignatureMethod(), true);
        }
        if (!out.containsKey(OAuth.OAUTH_TIMESTAMP)) {
            out.put(OAuth.OAUTH_TIMESTAMP, generateTimestamp(), true);
        }
        if (!out.containsKey(OAuth.OAUTH_NONCE)) {
            out.put(OAuth.OAUTH_NONCE, generateNonce(), true);
        }
        if (!out.containsKey(OAuth.OAUTH_VERSION)) {
            out.put(OAuth.OAUTH_VERSION, OAuth.VERSION_1_0, true);
        }
        if (!out.containsKey(OAuth.OAUTH_TOKEN)) {
            if ((token != null && !token.equals("")) || sendEmptyTokens) {
                out.put(OAuth.OAUTH_TOKEN, token, true);
            }
        }
    }

    @Override
    public HttpParameters getRequestParameters() {
        return requestParameters;
    }

    @Override
    public void setSendEmptyTokens(boolean enable) {
        this.sendEmptyTokens = enable;
    }

    protected void collectHeaderParameters(HttpRequest request, HttpParameters out) {
        HttpParameters headerParams = OAuth.oauthHeaderToParamsMap(request.getHeader(OAuth.HTTP_AUTHORIZATION_HEADER));
        out.putAll(headerParams, false);
    }

    protected void collectBodyParameters(HttpRequest request, HttpParameters out)
            throws IOException {

        // collect x-www-form-urlencoded body params
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith(OAuth.FORM_ENCODED)) {
            InputStream payload = request.getMessagePayload();
            out.putAll(OAuth.decodeForm(payload), true);
        }
    }

    protected void collectQueryParameters(HttpRequest request, HttpParameters out) {

        String url = request.getRequestUrl();
        int q = url.indexOf('?');
        if (q >= 0) {
            // Combine the URL query string with the other parameters:
            out.putAll(OAuth.decodeForm(url.substring(q + 1)), true);
        }
    }

    protected String generateTimestamp() {
        return Long.toString(System.currentTimeMillis() / TimeUnit.SECONDS.toMillis(1));
    }

    protected String generateNonce() {
        return Long.toString(random.nextLong());
    }
}
