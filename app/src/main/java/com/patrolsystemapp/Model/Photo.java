package com.patrolsystemapp.Model;

import java.io.Serializable;

public class Photo implements Serializable {
    private String id;
    private String url;

    public Photo(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
