package com.example.mobile_phone_subscription;

/**
 * A csomagok (Plan) adatait tároló osztály.
 */
public class Plan {
    private String id, description, imageUrl, details, name;
    private int price, subscribers;

    public Plan() {
        // Default constructor required for Firestore
    }

    public Plan(String name, String details, int price, String imageUrl) {
        this.name = name;
        this.details = details;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = "leiras";
        this.subscribers = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public int getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }
}