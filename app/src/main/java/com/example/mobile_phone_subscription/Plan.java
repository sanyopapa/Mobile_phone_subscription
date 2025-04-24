package com.example.mobile_phone_subscription;

    public class Plan {
        private String id;
        private String name;
        private String details;
        private int price;
        private String imageUrl;
        private String description;

        public Plan() {
            // Default constructor required for Firestore
        }

        public Plan(String name, String details, int price, String imageUrl) {
            this.name = name;
            this.details = details;
            this.price = price;
            this.imageUrl = imageUrl;
            this.description = "leiras";
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

        public String getDescription() {return description;}

        public void setDescription(String description) {this.description = description;}
    }