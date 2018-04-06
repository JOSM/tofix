package org.openstreetmap.josm.plugins.tofix.oauth.signpost;

import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpRequest;
import org.openstreetmap.josm.plugins.tofix.oauth.signpost.http.HttpResponse;

public interface OAuthProviderListener {

    void prepareRequest(HttpRequest request) throws Exception;

    void prepareSubmission(HttpRequest request) throws Exception;

    boolean onResponseReceived(HttpRequest request, HttpResponse response) throws Exception;
}
