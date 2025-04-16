package com.example.mobile_phone_subscription;

public class Plan {
    private String name;
    private String details;
    private double price;
    private String imageUrl;

    public Plan(String name, String details, double price, String imageUrl) {
        this.name = name;
        this.details = details;
        this.price = price;
        this.imageUrl = imageUrl;
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

    public String getImageUrl(){return imageUrl;}
}