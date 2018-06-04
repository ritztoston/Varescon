package com.parse.starter.varescon.Model;

/**
 * Created by iSwear on 12/24/2017.
 */

public class Notification {
    public String title;
    public String body;

    public Notification(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String detail) {
        this.body = detail;
    }
}
