package com.example.riley.currencyconverter.ItemListActivity;

public class ItemEntry {
    private String name;
    private String description;
    private double cost;
    private String created;
    private double latitude;
    private double longitude;
    private String type;

    public ItemEntry(String name, String description, double cost, String created, double latitude,
                     double longitude, String type) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.created = created;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getCost() {
        return cost;
    }

    public String getCreated() {
        return created;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getType() { return type; }
}
