package com.example.riley.currencyconverter.FullItemInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.riley.currencyconverter.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class FullItemInfoActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final long ZOOM = 15;
    private String localCurrency;
    private double rate;
    private String[] itemInfo;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_full_item_info);
        if (getIntent().getExtras() != null) {
            this.localCurrency = getIntent().getExtras().getString(getString(R.string.local_currency));
            this.rate = getIntent().getExtras().getDouble(getString(R.string.item_rate));
            this.itemInfo = getIntent().getExtras().getStringArray(getString(R.string.item_info));
            populateItemInfo();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_item_purchase_location);
            mapFragment.getMapAsync(this);
        }
    }

    private void populateItemInfo() {
        String name = itemInfo[0];
        String description = itemInfo[1];
        double localCost = Double.parseDouble(itemInfo[2]);
        String dateAdded = itemInfo[3];
        String type = itemInfo[6];
        ImageView typeImage = findViewById(R.id.item_type_image);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE);
        String defaultCurrency = preferences.getString(getString(R.string.preferences_default_currency), "USD");
        if (defaultCurrency.equals("USD")) {
            defaultCurrency = "$";
        }

        ((TextView) findViewById(R.id.item_name)).setText(name);
        if (description.isEmpty()) {
            findViewById(R.id.card_item_description).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.item_description)).setText(description);
        }
        ((TextView) findViewById(R.id.item_date_added)).setText(dateAdded);
        ((TextView) findViewById(R.id.item_local_cost)).setText(getString(R.string.total_format,
                String.format(Locale.US, "%.2f", localCost),
                localCurrency));
        ((TextView) findViewById(R.id.item_default_cost)).setText(getString(R.string.total_format,
                String.format(Locale.US, "%.2f", localCost / rate),
                defaultCurrency));

        switch (type) {
            case "Entertainment":
                typeImage.setImageResource(R.drawable.ic_local_activity_black_36dp);
                break;
            case "Food":
                typeImage.setImageResource(R.drawable.ic_local_hospital_black_36dp);
                break;
            case "Gift":
                typeImage.setImageResource(R.drawable.ic_card_giftcard_black_36dp);
                break;
            case "Health":
                typeImage.setImageResource(R.drawable.ic_local_hospital_black_36dp);
                break;
            case "Souvenir":
                typeImage.setImageResource(R.drawable.ic_beach_access_black_36dp);
                break;
            case "Transportation":
                typeImage.setImageResource(R.drawable.ic_directions_bus_black_36dp);
                break;
            case "Other":
                typeImage.setImageResource(R.drawable.ic_shopping_cart_black_36dp);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        double latitude = Double.parseDouble(itemInfo[4]);
        double longitude = Double.parseDouble(itemInfo[5]);
        System.err.println(latitude + " : " + longitude);
        LatLng purchaseLocation = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(purchaseLocation).title("TEST")).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        CameraUpdate locate = CameraUpdateFactory.newLatLng(purchaseLocation);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(ZOOM);
        googleMap.moveCamera(locate);
        googleMap.animateCamera(zoom);
    }
}
