package com.example.riley.currencyconverter.ItemListActivity;

public class ItemEntry {
    private String name;
    private String description;
    private double cost;
    private String created;
    private double latitude;
    private double longitude;
    private String type;
    private String key;

    public ItemEntry(String name, String description, double cost, String created, double latitude,
                     double longitude, String type, String key) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.created = created;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.key = key;
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

    public String getKey() { return key; }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setType(String type) {
        this.type = type;
    }
}
