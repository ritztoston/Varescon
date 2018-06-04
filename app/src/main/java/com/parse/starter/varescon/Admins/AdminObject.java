package com.parse.starter.varescon.Admins;

/**
 * Created by iSwear on 1/7/2018.
 */

public class AdminObject {
    private String email;
    private String name;
    private String adminId;

    public AdminObject(String email, String name, String adminId) {
        this.email = email;
        this.name = name;
        this.adminId = adminId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getAdminId() {
        return adminId;
    }
}
