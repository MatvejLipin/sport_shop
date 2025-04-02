package com.example.shop;

import java.util.Objects;

public class Product {
    private int productId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String manufacturerName;
    private String manufacturerCountry;
    private String manufacturerWebsite;
    private int stockQuantity; // Add stockQuantity field
    private String sportName; // Add sportName field

    // Updated constructor to include sportName
    public Product(int productId, String name, String description, double price, String imageUrl,
                   String manufacturerName, String manufacturerCountry, String manufacturerWebsite, int stockQuantity, String sportName) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.manufacturerName = manufacturerName;
        this.manufacturerCountry = manufacturerCountry;
        this.manufacturerWebsite = manufacturerWebsite;
        this.stockQuantity = stockQuantity; // Initialize stockQuantity
        this.sportName = sportName; // Initialize sportName
    }

    // Getter and setter for sportName
    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    // Getter and setter for stockQuantity
    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // Other getters
    public int getId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getManufacturerName() { return manufacturerName; }
    public String getManufacturerCountry() { return manufacturerCountry; }
    public String getManufacturerWebsite() { return manufacturerWebsite; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return productId == product.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
