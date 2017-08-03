package org.openstreetmap.josm.plugins.tofix.controller;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.openstreetmap.josm.plugins.tofix.bean.AccessToTask;
import org.openstreetmap.josm.plugins.tofix.bean.ResponseBean;
import org.openstreetmap.josm.plugins.tofix.bean.TaskCompleteBean;
import org.openstreetmap.josm.plugins.tofix.bean.items.Item;
import org.openstreetmap.josm.plugins.tofix.bean.items.ItemOsmlintLinestring;
import org.openstreetmap.josm.plugins.tofix.bean.items.ItemOsmlintMultilinestring;
import org.openstreetmap.josm.plugins.tofix.bean.items.ItemOsmlintMultipoint;
import org.openstreetmap.josm.plugins.tofix.bean.items.ItemOsmlintMultipolygon;
import org.openstreetmap.josm.plugins.tofix.bean.items.ItemOsmlintPoint;
import org.openstreetmap.josm.plugins.tofix.bean.items.ItemOsmlintPolygon;
import org.openstreetmap.josm.plugins.tofix.util.Request;
import org.openstreetmap.josm.plugins.tofix.util.Util;

/**
 *
 * @author ruben
 */
public class ItemController {

    Item item = new Item();
    ResponseBean responseBean = new ResponseBean();
    JsonArray relation;
    AccessToTask accessToTask;

    public AccessToTask getAccessToTask() {
        return accessToTask;
    }

    public void setAccessToTask(AccessToTask accessToTask) {
        this.accessToTask = accessToTask;
    }

    public JsonArray getRelation() {
        return relation;
    }

    public void setRelation(JsonArray relation) {
        this.relation = relation;
    }

    public Item getItem() {

        try {
            responseBean = Request.sendPOST(accessToTask.getTask_url());
            item.setStatus(responseBean.getStatus());
            Util.print(responseBean.getValue());
            JsonReader reader = Json.createReader(new StringReader(responseBean.getValue()));
            JsonObject object = reader.readObject();

            switch (responseBean.getStatus()) {
                case HttpURLConnection.HTTP_OK:
                    JsonObject geometry = (JsonObject) object.get("geometry");
                    JsonObject properties = (JsonObject) object.get("properties");
                    item.setType(geometry.getString("type"));

                    if (properties.containsKey("relations")) {
                        JsonArray relation2 = properties.getJsonArray("relations");
                        setRelation(relation2);
                    }

                    if (geometry.getString("type").equals("Point")) {
                        ItemOsmlintPoint iop = new ItemOsmlintPoint();
                        iop.setKey(properties.getString("_key"));

                        if (geometry.containsKey("type")) {
                            if (properties.containsKey("_osmId")) {
                                iop.setWay(convert(properties));
                            }
                            iop.setGeometry(geometry.getJsonString("type").toString());
                            iop.setCoordinates(geometry.get("coordinates").toString());
                            item.setItemOsmlintPoint(iop);
                        } else {
                            item.setStatus(520);
                        }
                    }
                    if (geometry.getString("type").equals("MultiPoint")) {
                        ItemOsmlintMultipoint iom = new ItemOsmlintMultipoint();
                        iom.setKey(properties.getString("_key"));
                        if (geometry.containsKey("type")) {
                            if (properties.containsKey("_osmId")) {
                                iom.setWay(convert(properties));
                            }
                            iom.setGeometry(geometry.getJsonString("type").toString());
                            iom.setCoordinates(geometry.get("coordinates").toString());
                            item.setItemOsmlintMultipoint(iom);
                        } else {
                            item.setStatus(520);
                        }

                    }
                    if (geometry.getString("type").equals("LineString")) {
                        ItemOsmlintLinestring iol = new ItemOsmlintLinestring();
                        iol.setKey(properties.getString("_key"));
                        if (properties.containsKey("_osmId")) {
                            iol.setWay(convert(properties));
                        }
                        if (object.containsKey("geometry")) {
                            iol.setGeometry(geometry.getJsonString("type").toString());
                            iol.setCoordinates(geometry.get("coordinates").toString());
                            item.setItemOsmlintLinestring(iol);
                        } else {
                            item.setStatus(520);
                        }
                    }
                    if (geometry.getString("type").equals("MultiLineString")) {
                        ItemOsmlintMultilinestring ioml = new ItemOsmlintMultilinestring();
                        ioml.setKey(properties.getString("_key"));
                        if (properties.containsKey("_osmId")) {
                            ioml.setWay(convert(properties));
                        }
                        if (object.containsKey("geometry")) {
                            ioml.setGeometry(geometry.getJsonString("type").toString());
                            ioml.setCoordinates(geometry.get("coordinates").toString());
                            item.setItemOsmlintMultilinestring(ioml);
                        } else {
                            item.setStatus(520);
                        }
                    }
                    if (geometry.getString("type").equals("Polygon")) {
                        ItemOsmlintPolygon iop = new ItemOsmlintPolygon();
                        iop.setKey(properties.getString("_key"));
                        if (properties.containsKey("_osmId")) {
                            iop.setWay(convert(properties));
                        }
                        if (object.containsKey("geometry")) {
                            iop.setGeometry(geometry.getJsonString("type").toString());
                            iop.setCoordinates(geometry.get("coordinates").toString());
                            item.setItemOsmlintPolygon(iop);
                        } else {
                            item.setStatus(520);
                        }
                    }
                    if (geometry.getString("type").equals("MultiPolygon")) {
                        ItemOsmlintMultipolygon iomp = new ItemOsmlintMultipolygon();
                        iomp.setKey(properties.getString("_key"));
                        if (properties.containsKey("_osmId")) {
                            iomp.setWay(convert(properties));
                        }
                        if (object.containsKey("geometry")) {
                            iomp.setGeometry(geometry.getJsonString("type").toString());
                            iomp.setCoordinates(geometry.get("coordinates").toString());
                            item.setItemOsmlintMultipolygon(iomp);
                        } else {
                            item.setStatus(520);
                        }
                    }
                    break;

                case 410:
                    TaskCompleteBean taskCompleteBean = new TaskCompleteBean();
                    taskCompleteBean.setTotal(0);
                    item.setTaskCompleteBean(taskCompleteBean);
                    break;
                case 503:
                    //Servidor en mantenimiento
                    break;
            }

            reader.close();

        } catch (Exception ex) {
            Util.alert(ex);
            Logger.getLogger(ItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return item;
    }

    public Long convert(JsonObject jo) {
        if (jo.get("_osmId").getValueType().compareTo(JsonValue.ValueType.STRING) == 0) {
            return Long.parseLong(jo.getString("_osmId"));
        } else {
            return Long.parseLong(jo.getJsonNumber("_osmId").toString());
        }
    }
}
