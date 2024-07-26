package org.openstreetmap.josm.plugins.tofix.bean;

import jakarta.json.JsonObject;

/**
 *
 * @author ruben
 */
public class ProjectBean {

    private String id;
    private String name;
    private JsonObject metadata;
    private String quadkey_set_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonObject getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonObject metadata) {
        this.metadata = metadata;
    }

    public String getQuadkey_set_id() {
        return quadkey_set_id;
    }

    public void setQuadkey_set_id(String quadkey_set_id) {
        this.quadkey_set_id = quadkey_set_id;
    }

    public String getChangesetComment() {
        if (!metadata.isNull("changesetComment")) {
            return metadata.getString("changesetComment");
        }
        return "none";
    }

}
