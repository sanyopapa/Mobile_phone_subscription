package com.example.mobile_phone_subscription;

public class Plan {
    private String name;
    private String details;
    private double price;

    public Plan(String name, String details, double price) {
        this.name = name;
        this.details = details;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public double getPrice() {
        return price;
    }
}