package com.example.riley.currencyconverter.MainActivity;

public class ListEntry {

    private String name;
    private String description;
    private String localCurrency;
    private String defaultCurrency;
    private double total;
    private String modified;
    private String created;

    ListEntry(String name, String description, String localCurrency, String defaultCurrency,
              double total, String modified, String created) {
        this.name = name;
        this.description = description;
        this.localCurrency = localCurrency;
        this.defaultCurrency = defaultCurrency;
        this.total = total;
        this.modified = modified;
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocalCurrency() {
        return localCurrency;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public double getTotal() {
        return total;
    }

    public String getModified() {
        return modified;
    }

    public String getCreated() {
        return created;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocalCurrency(String localCurrency) {
        this.localCurrency = localCurrency;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }
}
