package com.parse.starter.varescon.Model;

/**
 * Created by iSwear on 12/24/2017.
 */

public class Sender {
    public Notification notification;
    public String to;

    public Sender() {
    }

    public Sender(String to,Notification notification) {
        this.notification = notification;
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
