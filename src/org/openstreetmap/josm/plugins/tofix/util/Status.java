package org.openstreetmap.josm.plugins.tofix.util;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

/**
 *
 * @author ruben
 */
public class Status {

    public static boolean serverStatus() {
        try {
            String responseString = Request.sendGET(Config.getHOST());
            JsonReader reader = Json.createReader(new StringReader(responseString));
            JsonObject statusObject = reader.readObject();
            return "OK".equals(statusObject.getString("status"));
        } catch (IOException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public static boolean isInternetReachable() {
        try {
            HttpClient.create(new URL(Config.URL_OSM)).connect().disconnect();
            return true;
        } catch (IOException e) {
            Logging.log(Logging.LEVEL_ERROR, "Couldn't connect to the osm server. Please check your internet connection.", e);
            return false;
        }
    }
}
