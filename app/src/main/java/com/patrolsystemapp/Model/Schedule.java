package com.patrolsystemapp.Model;

public class Schedule {
    public String time, location, status;

    public Schedule(String time, String location, String status) {
        this.time = time;
        this.location = location;
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }
}
