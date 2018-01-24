package org.openstreetmap.josm.plugins.tofix.controller;

import java.io.IOException;
import java.io.StringReader;
import java.text.Collator;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

import org.openstreetmap.josm.plugins.tofix.bean.ListProjectBean;
import org.openstreetmap.josm.plugins.tofix.bean.ProjectBean;
import org.openstreetmap.josm.plugins.tofix.util.Config;
import org.openstreetmap.josm.plugins.tofix.util.Request;
import org.openstreetmap.josm.plugins.tofix.util.Util;

/**
 *
 * @author ruben
 */
public class ListProjectsController {

    private final ListProjectBean listProjectsBean = new ListProjectBean();
    private final String url;

    public ListProjectsController() {
        this.url = Config.getHOST() + "/" + Config.API_VERSION + "/projects";
    }

    public ListProjectBean getListProjects() {
        List<ProjectBean> projects = new LinkedList<>();
        try (JsonReader jsonReader = Json.createReader(new StringReader(Request.sendGET(url)))) {
            JsonArray jsonArray = jsonReader.readArray();
            for (JsonValue value : jsonArray) {
                ProjectBean projectBean = new ProjectBean();
                try (JsonReader jsonReader2 = Json.createReader(new StringReader(value.toString()))) {
                    JsonObject jsonProject = jsonReader2.readObject();
                    projectBean.setId(jsonProject.getString("id"));
                    projectBean.setName(jsonProject.getString("name"));
                    projectBean.setMetadata(jsonProject.getJsonObject("metadata"));
//                    projectBean.setQuadkey_set_id(jsonProject.getString("quadkey_set_id")); //FIXME
                }
                projects.add(projectBean);
            }
        } catch (IOException | NullPointerException | JsonParsingException ex) {
            Logger.getLogger(ListProjectsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Collections.sort(projects, (o1, o2) -> Collator.getInstance().compare(o1.getName(), o2.getName()));
        listProjectsBean.setProjects(projects);
        return listProjectsBean;
    }
}
