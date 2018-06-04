package com.parse.starter.varescon.Drivers;

/**
 * Created by iSwear on 1/7/2018.
 */

public class DriverObject {
    private String driverId;
    private String driverName;
    private String rating;
    private String assigned;

    public DriverObject(String driverId, String driverName, String rating, String assigned) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.rating = rating;
        this.assigned = assigned;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getAssigned() {
        return assigned;
    }
}
