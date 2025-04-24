package com.example.mobile_phone_subscription;

public class User {
    public String name;
    public String phone;

    public boolean admin;
    public User() {
    }

    public User(String name, String phone, boolean admin) {
        this.name = name;
        this.phone = phone;
        this.admin = admin;
    }
}