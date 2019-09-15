package com.patrolsystemapp.Model;

public class Schedule {
    public String id;
    public String room;
    public String time_start;
    public String time_end;
    public String date;
    public String status_node;
    public String message;
    public String scan_time;

    public Schedule(String id, String room, String time_start, String time_end, String date, String status_node, String message, String scan_time) {
        this.id = id;
        this.room = room;
        this.time_start = time_start;
        this.time_end = time_end;
        this.date = date;
        this.status_node = status_node;
        this.message = message;
        this.scan_time = scan_time;
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

    public String getStatus_node() {
        return status_node;
    }

    public void setStatus_node(String status_node) {
        this.status_node = status_node;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getScan_time() {
        return scan_time;
    }

    public void setScan_time(String scan_time) {
        this.scan_time = scan_time;
    }
}
