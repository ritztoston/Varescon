package com.parse.starter.varescon.DriverReportsRecycler;

/**
 * Created by iSwear on 1/7/2018.
 */

public class DriverReportObject {
    private String rideId;
    private String amt_paid;
    private String salary;
    private String date;

    public DriverReportObject(String rideId, String amt_paid, String salary, String date) {
        this.rideId = rideId;
        this.amt_paid = amt_paid;
        this.salary = salary;
        this.date =  date;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getAmt_paid() {
        return amt_paid;
    }

    public void setAmt_paid(String amt_paid) {
        this.amt_paid = amt_paid;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
