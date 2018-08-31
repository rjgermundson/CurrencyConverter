package com.example.riley.currencyconverter.MainActivity;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import com.example.riley.currencyconverter.R;

import java.io.IOException;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class GetLocalCurrency {

    /**
     * Returns the local currency of the user based on GPS
     * @param activity Current activity
     * @return The local currency as determined by user's location, or defaults to preferred currency
     */
    public static String getLocalCurrency(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getApplication().getSystemService(LOCATION_SERVICE);
        SharedPreferences preferences = activity.getApplication().getSharedPreferences(activity.getApplication().getString(R.string.preferences_file),
                MODE_PRIVATE);
        String localCurrency = preferences.getString(activity.getApplication().getString(R.string.preferences_default_currency), "USD");
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // We can attempt to check for a local currency using the GPS
            Location lastLocation = null;
            if (locationManager != null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            Geocoder geocoder = new Geocoder(activity);
            if (lastLocation != null) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                    if (addresses.size() == 1) {
                        // There is a location which might have a usable currency
                        String countryCode = addresses.get(0).getCountryCode();
                        String[] countryCodes = activity.getApplication().getResources().getStringArray(R.array.country_codes);
                        for (int i = 0; i < countryCodes.length; i++) {
                            if (countryCodes[i].equals(countryCode)) {
                                String[] currencyFull = activity.getApplication().getResources().getStringArray(R.array.currency_names_full);
                                localCurrency = currencyFull[i];
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            String[] currencyAbbrevs = activity.getApplication().getResources().getStringArray(R.array.currency_names_abbrev);
            for (int i = 0; i < currencyAbbrevs.length; i++) {
                if (currencyAbbrevs[i].equals(localCurrency)) {
                    String[] currencyFull = activity.getApplication().getResources().getStringArray(R.array.currency_names_full);
                    localCurrency = currencyFull[i];
                    break;
                }
            }
        }
        return localCurrency;
    }
}
