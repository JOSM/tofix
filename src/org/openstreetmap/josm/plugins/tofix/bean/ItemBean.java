package org.openstreetmap.josm.plugins.tofix.bean;

import jakarta.json.JsonObject;

/**
 *
 * @author ruben
 */
public class ItemBean {

    private String id;
    private String project_id;
    private String pin;
    private String quadkey;
    private String instructions;
    private String createdBy;
    private JsonObject featureCollection;
    private String status;
    private String lockedTill;
    private String lockedBy;
    private String metadata;
    private String sort;

    private int StatusServer ;

    public int getStatusServer() {
        return StatusServer;
    }

    public void setStatusServer(int StatusServer) {
        this.StatusServer = StatusServer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getQuadkey() {
        return quadkey;
    }

    public void setQuadkey(String quadkey) {
        this.quadkey = quadkey;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public JsonObject getFeatureCollection() {
        return featureCollection;
    }

    public void setFeatureCollection(JsonObject featureCollection) {
        this.featureCollection = featureCollection;
    }

  
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLockedTill() {
        return lockedTill;
    }

    public void setLockedTill(String lockedTill) {
        this.lockedTill = lockedTill;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
