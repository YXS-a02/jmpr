package yxs.a02.mper.model;

import java.util.Date;

public class ApiEndpoint {
    private int id;
    private String name;
    private String url;
    private Date added;
    private boolean isOnline;
    private Date lastChecked;

    public ApiEndpoint() {}

    public ApiEndpoint(String name, String url) {
        this.name = name;
        this.url = url;
        this.added = new Date();
        this.isOnline = false;
        this.lastChecked = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Date getAdded() { return added; }
    public void setAdded(Date added) { this.added = added; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public Date getLastChecked() { return lastChecked; }
    public void setLastChecked(Date lastChecked) { this.lastChecked = lastChecked; }

    @Override
    public String toString() {
        String status = isOnline ? "🟢" : "🔴";
        return status + " " + name + " (" + url + ")";
    }
}