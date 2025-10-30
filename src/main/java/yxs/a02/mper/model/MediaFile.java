package yxs.a02.mper.model;

import java.io.Serializable;

public class MediaFile implements Serializable {
    private String name;
    private String path;
    private String type; // "audio" or "video"
    private long size;
    private Double duration;

    public MediaFile() {}

    public MediaFile(String name, String path, String type, long size) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = size;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }

    @Override
    public String toString() {
        return name;
    }
}