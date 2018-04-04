package org.openstreetmap.josm.plugins.tofix.oauth;

public class SessionId {

    private String id;
    private String token;
    private String userName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "SessionId{" + "id=" + id + ", token=" + token + ", userName=" + userName + '}';
    }
}
