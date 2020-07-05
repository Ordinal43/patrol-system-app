package com.patrolsystemapp.Models;

import java.io.Serializable;

public class Schedule implements Serializable {
    private String id;
    private String room;
    private String time_start;
    private String time_end;
    private String date;
    private String countScanned;
    private Scan last_scan;

    public Schedule(String id, String room, String time_start, String time_end, String date, String countScanned, Scan last_scan) {
        this.id = id;
        this.room = room;
        this.time_start = time_start;
        this.time_end = time_end;
        this.date = date;
        this.countScanned = countScanned;
        this.last_scan = last_scan;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCountScanned() {
        return countScanned;
    }

    public void setCountScanned(String countScanned) {
        this.countScanned = countScanned;
    }

    public Scan getLast_scan() {
        return last_scan;
    }

    public void setLast_scan(Scan last_scan) {
        this.last_scan = last_scan;
    }
}
