package com.parse.starter.varescon.History.HistoryRecycler;

/**
 * Created by iSwear on 1/7/2018.
 */

public class HistoryObject {
    private String rideId;
    private String date;
    private String time;
    private String mop;
    private String destination;
    private String pickupLocation;
    private String rating;
    private String driver;
    private String amount;

    public HistoryObject(String rideId, String date, String time, String mop, String destination, String pickupLocation, String rating, String driver, String amount) {
        this.rideId = rideId;
        this.date = date;
        this.time = time;
        this.mop = mop;
        this.destination = destination;
        this.pickupLocation = pickupLocation;
        this.rating = rating;
        this.driver = driver;
        this.amount = amount;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getMop() {
        return mop;
    }

    public String getDestination() {
        return destination;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getRating() {
        return rating;
    }

    public String getDriver() {
        return driver;
    }

    public String getAmount() {
        return amount;
    }
}
