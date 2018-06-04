package com.parse.starter.varescon.Model;

import android.net.Uri;

/**
 * Created by iSwear on 12/1/2017.
 */

public class User {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String profilePic;
    private String identity;
    private boolean hailed;
    private boolean online;
    private boolean assigned;

    public User() {
    }

    public User(String email, String password, String firstname, String lastname, String profilePic, String identity, boolean hailed, boolean online, boolean assigned) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profilePic = profilePic;
        this.identity = identity;
        this.hailed = hailed;
        this.online = online;
        this.assigned = assigned;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public boolean isHailed() {
        return hailed;
    }

    public void setHailed(boolean hailed) {
        this.hailed = hailed;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
}
