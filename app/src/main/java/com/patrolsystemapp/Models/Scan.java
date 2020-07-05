package com.patrolsystemapp.Models;

import java.io.Serializable;

public class Scan implements Serializable {
    private String id;
    private String shift_id;
    private String status_node_id;
    private String message;
    private String scan_time;

    public Scan(String id, String shift_id, String status_node_id, String message, String scan_time) {
        this.id = id;
        this.shift_id = shift_id;
        this.status_node_id = status_node_id;
        this.message = message;
        this.scan_time = scan_time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShift_id() {
        return shift_id;
    }

    public void setShift_id(String shift_id) {
        this.shift_id = shift_id;
    }

    public String getStatus_node_id() {
        return status_node_id;
    }

    public void setStatus_node_id(String status_node_id) {
        this.status_node_id = status_node_id;
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
