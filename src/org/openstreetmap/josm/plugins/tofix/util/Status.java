package org.openstreetmap.josm.plugins.tofix.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.*;
import javax.json.*;
import static org.openstreetmap.josm.gui.mappaint.mapcss.ExpressionFactory.Functions.tr;

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
            Logger.getLogger(Class.class.getName()).log(Level.INFO, "{0} -> {1} -> {2}", new Object[]{tr("API didn't response!"), url,con.getResponseCode()});
            return false;
        } catch (Exception ex) {
            Logger.getLogger(Class.class.getName()).log(Level.INFO, "{0} -> {1} ({2})", new Object[]{tr("API didn't response!"), url,tr("connection refused")});
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
