package com.example.shop;

public class Product {
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String manufacturerName;
    private String manufacturerCountry;
    private String manufacturerWebsite;

    public Product(String name, String description, double price, String imageUrl,
                   String manufacturerName, String manufacturerCountry, String manufacturerWebsite) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.manufacturerName = manufacturerName;
        this.manufacturerCountry = manufacturerCountry;
        this.manufacturerWebsite = manufacturerWebsite;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getManufacturerName() { return manufacturerName; }
    public String getManufacturerCountry() { return manufacturerCountry; }
    public String getManufacturerWebsite() { return manufacturerWebsite; }
}
