package com.example.riley.currencyconverter.MainActivity;

import android.os.Parcel;
import android.os.Parcelable;

public class ListEntry implements Parcelable {

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

    public ListEntry(Parcel in) {
        String[] data = new String[6];
        in.readStringArray(data);
        double total = in.readDouble();
        this.name = data[0];
        this.description = data[1];
        this.localCurrency = data[2];
        this.defaultCurrency = data[3];
        this.total = total;
        this.modified = data[4];
        this.created = data[5];
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {name, description, localCurrency,
                                            defaultCurrency, modified,
                                            created});
        dest.writeDouble(total);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ListEntry createFromParcel(Parcel in) {
            return new ListEntry(in);
        }

        public ListEntry[] newArray(int size) {
            return new ListEntry[size];
        }
    };
}
