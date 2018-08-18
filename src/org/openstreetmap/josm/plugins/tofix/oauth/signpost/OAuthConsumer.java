/* Copyright (c) 2009 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openstreetmap.josm.plugins.tofix.oauth.signpost;

import java.io.Serializable;

import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthCommunicationException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthExpectationFailedException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.exception.OAuthMessageSignerException;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpParameters;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpRequest;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.signature.OAuthMessageSigner;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.signature.SigningStrategy;

public interface OAuthConsumer extends Serializable {

    public void setMessageSigner(OAuthMessageSigner messageSigner);

    public void setAdditionalParameters(HttpParameters additionalParameters);

    public void setSigningStrategy(SigningStrategy signingStrategy);

    public void setSendEmptyTokens(boolean enable);

    public HttpRequest sign(HttpRequest request) throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException;

    public HttpRequest sign(Object request) throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException;

    public String sign(String url) throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException;

    public void setTokenWithSecret(String token, String tokenSecret);

    public String getToken();

    public String getTokenSecret();

    public String getConsumerKey();

    public String getConsumerSecret();

    public HttpParameters getRequestParameters();
}
