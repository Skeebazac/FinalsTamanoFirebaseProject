package com.eldroid.finalstamanofirebaseproject.Model;

public class User {
    private String user_id;
    private String user_fname;
    private String user_lname;
    private String user_mname;
    private String user_email;
    private String user_photo_url;
    private boolean user_first_register;

    public User() {
    }

    public User(String user_id, String user_fname, String user_lname, String user_mname, String user_email, String user_photo_url, boolean user_first_register) {
        this.user_id = user_id;
        this.user_fname = user_fname;
        this.user_lname = user_lname;
        this.user_mname = user_mname;
        this.user_email = user_email;
        this.user_photo_url = user_photo_url;
        this.user_first_register = user_first_register;
    }
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    public boolean isUser_first_register() {
        return user_first_register;
    }

    public void setUser_first_register(boolean user_first_register) {
        this.user_first_register = user_first_register;
    }

    public String getUser_fname() {
        return user_fname;
    }

    public void setUser_fname(String user_fname) {
        this.user_fname = user_fname;
    }

    public String getUser_lname() {
        return user_lname;
    }

    public void setUser_lname(String user_lname) {
        this.user_lname = user_lname;
    }

    public String getUser_mname() {
        return user_mname;
    }

    public void setUser_mname(String user_mname) {
        this.user_mname = user_mname;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_photo_url() {
        return user_photo_url;
    }

    public void setUser_photo_url(String user_photo_url) {
        this.user_photo_url = user_photo_url;
    }
}
