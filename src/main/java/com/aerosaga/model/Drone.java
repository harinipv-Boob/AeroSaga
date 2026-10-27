package com.aerosaga.model;

public class Drone {

private String droneId;
private String droneName;
private double batteryLevel;
private String status;

public Drone() {
}

public Drone(String droneId, String droneName, double batteryLevel, String status) {
    this.droneId = droneId;
    this.droneName = droneName;
    this.batteryLevel = batteryLevel;
    this.status = status;
}

public String getDroneId() {
    return droneId;
}

public void setDroneId(String droneId) {
    this.droneId = droneId;
}

public String getDroneName() {
    return droneName;
}

public void setDroneName(String droneName) {
    this.droneName = droneName;
}

public double getBatteryLevel() {
    return batteryLevel;
}

public void setBatteryLevel(double batteryLevel) {
    this.batteryLevel = batteryLevel;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}
}
