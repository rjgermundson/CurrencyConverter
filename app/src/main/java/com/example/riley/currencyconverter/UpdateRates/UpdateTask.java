package com.example.riley.currencyconverter.UpdateRates;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.riley.currencyconverter.CurrencyScraper.WebScraper;
import com.example.riley.currencyconverter.LocalStorage.SQLiteHelper;
import com.example.riley.currencyconverter.R;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 * Updates the local storage with new conversion rates
 */
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
        } catch (Exception e) {
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
        Hashtable<String, String> abbreviations = new Hashtable<>();
        String[] fullNames = activity.getResources().getStringArray(R.array.currency_names_full);
        String[] abbrevNames = activity.getResources().getStringArray(R.array.currency_names_abbrev);
        for (int i = 0; i < fullNames.length; i++) {
            abbreviations.put(fullNames[i], abbrevNames[i]);
        }
        return abbreviations;
    }
}
