package org.openstreetmap.josm.plugins.tofix.oauth.signpost;

import java.io.Serializable;
import java.util.Map;

import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthCommunicationException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthExpectationFailedException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthMessageSignerException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthNotAuthorizedException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpParameters;

public interface OAuthProvider extends Serializable {

    public String retrieveRequestToken(OAuthConsumer consumer, String callbackUrl,
            String... customOAuthParams) throws OAuthMessageSignerException,
            OAuthNotAuthorizedException, OAuthExpectationFailedException,
            OAuthCommunicationException;

    public void retrieveAccessToken(OAuthConsumer consumer, String oauthVerifier,
            String... customOAuthParams) throws OAuthMessageSignerException,
            OAuthNotAuthorizedException, OAuthExpectationFailedException,
            OAuthCommunicationException;

    public HttpParameters getResponseParameters();

    public void setResponseParameters(HttpParameters parameters);

    @Deprecated
    public void setRequestHeader(String header, String value);

    @Deprecated
    public Map<String, String> getRequestHeaders();

    public void setOAuth10a(boolean isOAuth10aProvider);

    public boolean isOAuth10a();

    public String getRequestTokenEndpointUrl();

    public String getAccessTokenEndpointUrl();

    public String getAuthorizationWebsiteUrl();

    public void setListener(OAuthProviderListener listener);

    public void removeListener(OAuthProviderListener listener);
}
