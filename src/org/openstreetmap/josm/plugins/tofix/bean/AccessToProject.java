package org.openstreetmap.josm.plugins.tofix.bean;

import org.openstreetmap.josm.plugins.tofix.util.Config;

/**
 *
 * @author ruben
 */
public class AccessToProject {

    private String project_id;
    private String project_name;
    private boolean access;
    private ProjectBean project;

    public ProjectBean getProject() {
        return project;
    }

    public void setProject(ProjectBean project) {
        this.project = project;
    }

    public AccessToProject(String task_id, boolean access) {
        this.project_id = task_id;
        this.access = access;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public boolean isAccess() {
        return access;
    }

    public void setAccess(boolean access) {
        this.access = access;
    }

    public String getProject_url() {
        String url = Config.getHOST() + "/" + Config.API_VERSION + "/projects/" + this.getProject_id() + "/items" + Config.getQUERY();
        return url;
    }
}
