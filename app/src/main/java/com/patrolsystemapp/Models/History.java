package com.patrolsystemapp.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class History implements Serializable {
    private String message;
    private String scan_time;
    private ArrayList<Photo> photos;

    public History(String message, String scan_time, ArrayList<Photo> photos) {
        this.message = message;
        this.scan_time = scan_time;
        this.photos = photos;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getScanTime() {
        return scan_time;
    }

    public void setScanTime(String scan_time) {
        this.scan_time = scan_time;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }
}