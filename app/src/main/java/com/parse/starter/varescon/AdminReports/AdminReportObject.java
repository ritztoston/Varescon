package com.parse.starter.varescon.AdminReports;

/**
 * Created by iSwear on 1/7/2018.
 */

public class AdminReportObject {
    private String rideId;
    private String amt_paid;
    private String salary;
    private String date;
    private String earning;

    public AdminReportObject(String rideId, String amt_paid, String salary, String date, String earning) {
        this.rideId = rideId;
        this.amt_paid = amt_paid;
        this.salary = salary;
        this.date = date;
        this.earning = earning;
    }

    public String getRideId() {
        return rideId;
    }

    public String getAmt_paid() {
        return amt_paid;
    }

    public String getSalary() {
        return salary;
    }

    public String getDate() {
        return date;
    }

    public String getEarning() {
        return earning;
    }
}
