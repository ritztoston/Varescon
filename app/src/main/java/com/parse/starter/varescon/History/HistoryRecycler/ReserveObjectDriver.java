package com.parse.starter.varescon.History.HistoryRecycler;

/**
 * Created by iSwear on 1/7/2018.
 */

public class ReserveObjectDriver {
    private String date;
    private String mop;
    private String time;
    private String progress;
    private String rideId;

    public ReserveObjectDriver(String rideId, String date, String mop, String time, String progress) {
        this.date = date;
        this.mop = mop;
        this.time = time;
        this.progress = progress;
        this.rideId = rideId;
    }

    public String getDate() {
        return date;
    }

    public String getMop() {
        return mop;
    }

    public String getTime() {
        return time;
    }

    public String getProgress() {
        return progress;
    }

    public String getRideId() {
        return rideId;
    }
}
