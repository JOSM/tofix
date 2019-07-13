package org.openstreetmap.josm.plugins.tofix.util;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        return testStatus(Config.getHOST());
    }
    
    public static boolean testStatus(String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.connect();
            if (con.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),StandardCharsets.UTF_8));
                String responseString = "";
                while (br.ready()) {
                    responseString = br.readLine();
                }
                JsonReader reader = Json.createReader(new StringReader(responseString));
                JsonObject statusObject = reader.readObject();
                Logger.getLogger(Class.class.getName()).log(Level.INFO, "{0} -> {1} -> {2}", new Object[]{tr("API server status : " + statusObject.getString("status")), url,con.getResponseCode()});
                return true;
            }
            Logger.getLogger(Class.class.getName()).log(Level.INFO, "{0} -> {1} -> {2}", new Object[]{tr("API did not respond!"), url,con.getResponseCode()});
            return false;
        } catch (Exception ex) {
            Logger.getLogger(Class.class.getName()).log(Level.INFO, "{0} -> {1} ({2})", new Object[]{tr("API did not respond!"), url,tr("connection refused")});
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
