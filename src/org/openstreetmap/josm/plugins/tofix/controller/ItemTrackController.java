package org.openstreetmap.josm.plugins.tofix.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.openstreetmap.josm.plugins.tofix.bean.ItemBean;
import org.openstreetmap.josm.plugins.tofix.util.Config;
import org.openstreetmap.josm.plugins.tofix.util.Request;

public class ItemTrackController {

    public void lockItem(ItemBean item, String lock) {
        //Create the URL
        String url = Config.getHOST() + "/" + Config.API_VERSION + "/projects/" + item.getProject_id() + "/items/" + item.getId();
        //Create data(json)to looked the item
        JsonObjectBuilder trackBeanBuilder = Json.createObjectBuilder();

        trackBeanBuilder
                .add("lock", lock);

        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();
        metadataBuilder.add("editor", "JOSM");
        trackBeanBuilder.add("metadata", metadataBuilder);
        JsonObject track_edit = trackBeanBuilder.build();
        try {
            Request.sendPUT_Json(url, track_edit.toString());
        } catch (IOException ex) {
            Logger.getLogger(ItemTrackController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateStatusItem(ItemBean item, String status) {
        //Create the URL
        String url = Config.getHOST() + "/" + Config.API_VERSION + "/projects/" + item.getProject_id() + "/items/" + item.getId();
        //Create data(json)to looked the item
        JsonObjectBuilder trackBeanBuilder = Json.createObjectBuilder();

        trackBeanBuilder
                .add("status", status);

        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();
        metadataBuilder.add("editor", "JOSM");
        trackBeanBuilder.add("metadata", metadataBuilder);
        JsonObject track_edit = trackBeanBuilder.build();
        try {
            Request.sendPUT_Json(url, track_edit.toString());
        } catch (IOException ex) {
            Logger.getLogger(ItemTrackController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
