package com.patrolsystemapp.Model;

import java.io.Serializable;

public class Schedule implements Serializable {
    public String id;
    public String room;
    public String time_start;
    public String time_end;
    public String date;
    public String countScanned;

    public Schedule(String id, String room, String time_start, String time_end, String date, String countScanned) {
        this.id = id;
        this.room = room;
        this.time_start = time_start;
        this.time_end = time_end;
        this.date = date;
        this.countScanned = countScanned;
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
}
