package org.openstreetmap.josm.plugins.tofix.controller;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import org.openstreetmap.josm.plugins.tofix.bean.AccessToProject;
import org.openstreetmap.josm.plugins.tofix.bean.ItemBean;
import org.openstreetmap.josm.plugins.tofix.util.Request;

/**
 *
 * @author ruben
 */
public class ItemController {

    ItemBean item = new ItemBean();
    JsonArray relation;
    AccessToProject accessToProject;

    public AccessToProject getAccessToProject() {
        return accessToProject;
    }

    public void setAccessToProject(AccessToProject accessToProject) {
        this.accessToProject = accessToProject;
    }

    public JsonArray getRelation() {
        return relation;
    }

    public void setRelation(JsonArray relation) {
        this.relation = relation;
    }

    public ItemBean getItem() {
        try {
            //Get item as String
            String itemString = Request.sendGET(accessToProject.getProject_url());
            JsonReader reader = Json.createReader(new StringReader(itemString));
            JsonArray arrayItems = reader.readArray();
            //Check if the array hays items
            if (arrayItems.size() > 0) {
                JsonObject itemObject = arrayItems.getJsonObject(0);
                item.setId(itemObject.getString("id"));
                item.setProject_id(itemObject.getString("project_id"));
                item.setFeatureCollection(itemObject.getJsonObject("featureCollection"));
                item.setStatusServer(200);

            } else {
                item.setStatusServer(410);
            }
            reader.close();
        } catch (Exception ex) {
            item.setStatusServer(502);
            Logger.getLogger(ItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return item;
    }
}
