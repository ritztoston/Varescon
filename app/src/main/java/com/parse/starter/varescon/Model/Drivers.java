package com.parse.starter.varescon.Model;

/**
 * Created by iSwear on 12/1/2017.
 */

public class Drivers {
    private String uid;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String profilepic;
    private String status;
    private boolean hailed;

    public Drivers() {
    }

    public Drivers(String uid, String email, String password, String firstname, String lastname, String status, String profilepic, boolean hailed) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.status = status;
        this.profilepic = profilepic;
        this.hailed = hailed;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String name) {
        this.firstname = name;
    }

    public String getProfilePic() {
        return profilepic;
    }

    public void setProfilePic(String profilePic) {
        this.profilepic = profilePic;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean getHailed() {
        return hailed;
    }

    public void setHailed(boolean hailed) {
        this.hailed = hailed;
    }
}
