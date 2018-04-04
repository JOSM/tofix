package org.openstreetmap.josm.plugins.tofix.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.Logging;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Config {

    public static final String API_VERSION = "v1";
    public static final String URL_TOFIX = "http://osmlab.github.io/to-fix/";
    public static final String URL_OSM = "http://www.openstreetmap.org";
    public static final String URL_TOFIX_ISSUES = "https://github.com/JOSM/tofix/issues";
    public static final String DEFAULT_API_HOST = "http://tofix.devseed.com:8000";
    public static String QUERY;
    public static final String DEFAULT_QUERY = "?status=open&lock=unlocked&page_size=1&fc=true&random=true";
    public static String BBOX = "none";

    public static final int GET = 0, UPDATE = 1, ADD = 2, REMOVE = 3;
    public static final String DEFAULT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6IjgwNTIxNTEiLCJ1c2VybmFtZSI6InRvZml4am9zbSIsImltYWdlIjoiIn0.05dWZsiq1aSxlPqP12oUdWEuyBI4nRh1zEZrBu6iBKA";
    private static final String PREFERENCES_FILE = "preferences.xml";
    private static final String PLUGIN_PREFERENCES_FILE = "plugin_preferences.xml";

    public static String getQUERY() {
        if (BBOX.equals("none")) {
            QUERY = DEFAULT_QUERY;
        } else {
            QUERY = DEFAULT_QUERY + "&bbox=" + BBOX;
        }
        return QUERY;
    }

    public static void setBBOX(String bbox) {
        BBOX = bbox;
    }

    public static String getHOST() {
        if (!getPluginPreferencesFile().exists()) {
            preparePluginPreferencesFile();
        }
        Object r = preferences(GET, new String[]{"tofix-server.host"}, getPluginPreferencesFile().getAbsolutePath());
        return (r != null) ? r.toString() : Config.preferences(Config.ADD, new String[]{"tofix-server.host", DEFAULT_API_HOST}, Config.getPluginPreferencesFile().getAbsolutePath()).toString();
    }

    public static String getTOKEN() {
        if (!getPluginPreferencesFile().exists()) {
            preparePluginPreferencesFile();                        
            Config.preferences(Config.ADD, new String[]{"tofix-server.token", DEFAULT_TOKEN}, Config.getPluginPreferencesFile().getAbsolutePath()).toString();       
            
            return DEFAULT_TOKEN;
        }else{  
            Object h = preferences(GET, new String[]{"tofix-server.host"}, getPluginPreferencesFile().getAbsolutePath());
            
            if (!Config.DEFAULT_API_HOST.equals(h.toString()) && !Status.testStatus(h.toString())) {
                Config.preferences(Config.UPDATE, new String[]{"tofix-server.host", DEFAULT_API_HOST}, Config.getPluginPreferencesFile().getAbsolutePath());
                Config.preferences(Config.UPDATE, new String[]{"tofix-server.token", DEFAULT_TOKEN}, Config.getPluginPreferencesFile().getAbsolutePath());
                return DEFAULT_TOKEN;
            }else{
                Object t = preferences(GET, new String[]{"tofix-server.token"}, getPluginPreferencesFile().getAbsolutePath());
                return t.toString();
            }      
        }
    }

    public static String getAPILogin() {
        return getHOST() + "/" + API_VERSION + "/" + "auth/openstreetmap";
    }

    public static boolean isURL(String s) {
        try {
            Pattern patt = Pattern.compile("\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
            Matcher matcher = patt.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isDefaultToken(String t) {
        return t.equals(DEFAULT_TOKEN);
    }

    public static boolean isDefaultAPI(String a) {
        return a.equals(DEFAULT_API_HOST);
    }

    public static String getUserName() {
        Object r = preferences(GET, new String[]{"osm-server.username"});
        return (r != null) ? r.toString() : "";//tofixjosm
    }

    public static String getPassword() {
        Object r = preferences(GET, new String[]{"osm-server.password"});
        return (r != null) ? r.toString() : "";
    }

    public static File getCurrentDirectory() {
        return new File(System.getProperty("user.dir"));
    }
    @SuppressWarnings("deprecation")
    public static File getPreferencesDirectory() {
        return Main.pref.getPreferencesDirectory();
    }

    public static File getCacheDirectory() {
        return new File(getPreferencesDirectory().getAbsolutePath() + File.separator + "cache");
    }

    public static File getPluginsDirectory() {
        return new File(getPreferencesDirectory().getAbsolutePath() + File.separator + "plugins");
    }

    public static File getRemoteControl() {
        return new File(getPreferencesDirectory().getAbsolutePath() + File.separator + "remotecontrol");
    }

    public static File getPreferencesFile() {
        return new File(getPreferencesDirectory().getAbsolutePath() + File.separator + PREFERENCES_FILE);
    }

    public static File getPluginPreferencesFile() {
        return new File(getPreferencesDirectory().getAbsolutePath() + File.separator + PLUGIN_PREFERENCES_FILE);
    }

    public static Object preferences(int op, String[] parms, String... path) {
        if (path != null && path.length > 0) {
            return preferences(path[0], op, parms);
        } else {
            return preferences(Config.getPreferencesFile().getAbsolutePath(), op, parms);
        }
    }

    private static Object preferences(String path, int op, String[] parms) {
        DocumentBuilder dBuilder;
        Object r = null;
        try {
            dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new File(path));
            doc.getDocumentElement().normalize();

            switch (op) {
                case GET:
                    r = get(doc, parms[0]);
                    break;
                case ADD:
                    r = add(doc, parms[0], parms[1]);
                    break;
                case UPDATE:
                    r = update(doc, parms[0], parms[1]);
                    break;
                case REMOVE:
                    r = remove(doc, parms[0]);
                    break;
            }
            doc.getDocumentElement().normalize();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

        } catch (Exception e) {
            Logging.error(e);
        }
        return r;
    }

    private static String get(Document doc, String key) throws Exception {
        NodeList nList = doc.getElementsByTagName("tag");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("key").equals(key)) {
                    return eElement.getAttribute("value");
                }
            }
        }
        return null;
    }

    private static String add(Document doc, String key, String value) {
        NodeList p = doc.getElementsByTagName("preferences");
        Element e1;
        for (int i = 0; i < p.getLength(); i++) {
            e1 = (Element) p.item(i);
            Element e = doc.createElement("tag");
            e.setAttribute("key", key);
            e.setAttribute("value", value);
            e1.appendChild(e);
        }
        return value;
    }

    private static String update(Document doc, String key, String value) {
        NodeList employees = doc.getElementsByTagName("tag");
        Element e = null;
        for (int i = 0; i < employees.getLength(); i++) {
            e = (Element) employees.item(i);
            if (e.getNodeType() == Node.ELEMENT_NODE) {
                if (e.getAttribute("key").equals(key)) {
                    e.setAttribute("value", value);
                    return "Success";
                }
            }
        }
        return "Success";
    }

    private static String remove(Document doc, String key) throws Exception {
        NodeList nList = doc.getElementsByTagName("preferences");
        NodeList nListCh = nList.item(0).getChildNodes();
        for (int i = 0; i < nListCh.getLength(); i++) {

            Node nNode = nListCh.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("key").equals(key)) {
                    nList.item(0).removeChild(nNode);
                    return "Success";
                }
            }
        }
        return null;
    }

    private static void preparePluginPreferencesFile() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("preferences");
            root.setAttribute("xmlns", "http://josm.openstreetmap.de/preferences-1.0");
            root.setAttribute("version", "13333");
            doc.appendChild(root);

            Element item = doc.createElement("tag");
            item.setAttribute("key", "tofix-client");
            item.setAttribute("value", "josm-plugin");
            root.appendChild(item);

            item = doc.createElement("tag");
            item.setAttribute("key", "credits");
            item.setAttribute("value", "Rub21,samely,ridixcr");
            root.appendChild(item);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(getPluginPreferencesFile());

            transformer.transform(source, result);
        } catch (Exception ex) {
            Logging.error(ex);
        }
    }
}
