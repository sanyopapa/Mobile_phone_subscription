package com.example.mobile_phone_subscription;

/**
 * A felhasználó profiljához kapcsolódó adatok tárolására szolgáló osztály
 */
public class User {
    public String name, phone, subscriptionId, subscriptionName;
    public boolean admin;
    public long subscriptionDate;
    public int subscriptionPrice;
    public User() {
    }

    public User(String name, String phone, boolean admin) {
        this.name = name;
        this.phone = phone;
        this.admin = admin;
        this.subscriptionId = null;
        this.subscriptionName = null;
        this.subscriptionDate = 0;
        this.subscriptionPrice = 0;
    }

    public void setSubscription(String planId, String planName, int price) {
        this.subscriptionId = planId;
        this.subscriptionName = planName;
        this.subscriptionDate = System.currentTimeMillis();
        this.subscriptionPrice = price;
    }

    public void cancelSubscription() {
        this.subscriptionId = null;
        this.subscriptionName = null;
        this.subscriptionDate = 0;
        this.subscriptionPrice = 0;
    }

    public boolean hasSubscription() {
        return subscriptionId != null && !subscriptionId.isEmpty();
    }
}