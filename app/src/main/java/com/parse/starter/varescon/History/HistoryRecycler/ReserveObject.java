package com.parse.starter.varescon.History.HistoryRecycler;

/**
 * Created by iSwear on 1/7/2018.
 */

public class ReserveObject {
    private String date;
    private String mop;
    private String time;
    private String progress;
    private String key;

    public ReserveObject(String date, String mop, String time, String progress, String key) {
        this.date = date;
        this.mop = mop;
        this.time = time;
        this.progress = progress;
        this.key = key;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMop() {
        return mop;
    }

    public void setMop(String mop) {
        this.mop = mop;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
