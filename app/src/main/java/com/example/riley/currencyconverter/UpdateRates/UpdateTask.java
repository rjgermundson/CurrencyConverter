package com.example.riley.currencyconverter.UpdateRates;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import com.example.riley.currencyconverter.CurrencyScraper.WebScraper;
import com.example.riley.currencyconverter.LocalStorage.SQLiteHelper;
import com.example.riley.currencyconverter.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class UpdateTask extends AsyncTask<Activity, String, Boolean> {

    private final WeakReference<Activity> activityWeakReference;

    public UpdateTask(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected Boolean doInBackground(Activity... params) {
        Activity activity = activityWeakReference.get();
        SharedPreferences preferences = activity.getSharedPreferences(activity.getString(R.string.preferences_file),
                                                               Context.MODE_PRIVATE);
        WebScraper webScraper = new WebScraper(getAbbreviations(activity),
                        preferences.getString(activity.getString(R.string.preferences_default_currency),
                        "USD"));
        webScraper.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            Hashtable<String, Double> result = webScraper.get();
            // Insert rates in database
            String[] ratesColumns = activity.getResources().getStringArray(R.array.rates_columns);
            String[] ratesTypes = activity.getResources().getStringArray(R.array.rates_types);
            SQLiteHelper helper = new SQLiteHelper(activity.getApplicationContext(),
                    activity.getString(R.string.rates_table),
                    ratesColumns,
                    ratesTypes);
            for (String currency : result.keySet()) {
                Double rate = result.get(currency);
                ContentValues contentValues = new ContentValues();
                contentValues.put(ratesColumns[0], currency);
                contentValues.put(ratesColumns[1], rate);
                helper.insertRecord(contentValues);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
        if (this.isCancelled()) {
            return true;
        }
        return true;
    }

    @Override
    public void onPostExecute(Boolean result) {
        Activity activity = activityWeakReference.get();
        if (activity != null
            && !activity.isFinishing()) {
            if (!result) {
                Toast.makeText(activity.getApplicationContext(), "Internet connection interrupted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Hashtable<String, String> getAbbreviations(Activity activity) {
        InputStream inputStream;
        try {
            inputStream = activity.getAssets().open("CurrencyAbbreviations");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Hashtable<String, String> abbreviations = new Hashtable<>();
        Scanner scanner = new Scanner(inputStream);

        // Get information for SQL insertions
        String[] abbrevColumns = activity.getResources().getStringArray(R.array.abbrev_columns);
        String[] abbrevTypes = activity.getResources().getStringArray(R.array.abbrev_types);
        SQLiteHelper helper = new SQLiteHelper(activity.getApplicationContext(),
                activity.getString(R.string.abbreviation_table),
                abbrevColumns,
                abbrevTypes);

        while (scanner.hasNextLine()) {
            // Get data from page
            String fullName = scanner.nextLine();
            String abbreviation = scanner.nextLine();
            String countryCode = scanner.nextLine();

            // Insert values into database
            ContentValues contentValues = new ContentValues();
            contentValues.put(abbrevColumns[0], fullName);
            contentValues.put(abbrevColumns[1], abbreviation);
            contentValues.put(abbrevColumns[2], countryCode);
            helper.insertRecord(contentValues);

            // Add abbreviations to local map
            abbreviations.put(fullName, abbreviation);
        }
        scanner.close();
        return abbreviations;
    }
}
