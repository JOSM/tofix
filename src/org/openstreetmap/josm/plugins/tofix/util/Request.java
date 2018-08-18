package org.openstreetmap.josm.plugins.tofix.util;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.HttpClient.Response;

/**
 *
 * @author ruben
 */
public class Request {
    
    public static void sendPOST_Json(String url, String object) throws IOException {
        HttpClient.create(new URL(url), "POST")
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", Config.getTOKEN())
                .setAccept("application/json")
                .setRequestBody(object.getBytes(StandardCharsets.UTF_8))
                .connect().disconnect();
    }

    public static void sendPUT_Json(String url, String object) throws IOException {
        HttpClient.create(new URL(url), "PUT")
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", Config.getTOKEN())
                .setAccept("application/json")
                .setRequestBody(object.getBytes(StandardCharsets.UTF_8))
                .connect().disconnect();
    }
    
    public static String sendGET(String url) throws IOException {
        Response response = HttpClient.create(new URL(url))
                .setHeader("Authorization",Config.getTOKEN())
                .connect();
        String result = response.fetchContent();
        response.disconnect();
        return result;
    }
}
