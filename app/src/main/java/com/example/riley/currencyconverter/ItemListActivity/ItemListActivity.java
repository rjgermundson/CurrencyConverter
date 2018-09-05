package com.example.riley.currencyconverter.ItemListActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riley.currencyconverter.LocalStorage.SQLiteHelper;
import com.example.riley.currencyconverter.MainActivity.GetLocalCurrency;
import com.example.riley.currencyconverter.R;
import com.example.riley.currencyconverter.Settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ItemListActivity extends AppCompatActivity {
    private String listName;
    private SQLiteHelper itemHelper;
    private String localCurrency;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_item_list);
        if (getIntent().getExtras() != null) {
            listName = getIntent().getExtras().getString(getApplicationContext().getString(R.string.list_name));
            localCurrency = getIntent().getExtras().getString(getApplicationContext().getString(R.string.local_currency));
        }
        recyclerView = findViewById(R.id.item_list_recycler);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        String[] itemColumns = getResources().getStringArray(R.array.items_columns);
        String[] itemTypes = getResources().getStringArray(R.array.items_types);
        itemHelper = new SQLiteHelper(getApplicationContext(), listName, itemColumns, itemTypes);
        setupListInfo();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                DialogFragment prev = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    fragmentTransaction.remove(prev);
                }
                DialogFragment dialogFragment = CreateItemDialogFragment.getInstance(localCurrency);
                dialogFragment.show(fragmentTransaction, CreateItemDialogFragment.TAG);
                getFragmentManager().executePendingTransactions();
                ((Toolbar) dialogFragment.getDialog().findViewById(R.id.toolbar)).getMenu()
                        .getItem(0).setOnMenuItemClickListener(
                        new CreateItemListener(dialogFragment.getDialog()));
                Button save = dialogFragment.getDialog().findViewById(R.id.create_item_button);
                save.setOnClickListener(new CreateItemListener(dialogFragment.getDialog()));
            }
        });
        ItemAdapter adapter = getItemAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Initializes the TextViews in the list information header in the activity
     */
    private void setupListInfo() {
        ((TextView) findViewById(R.id.list_item_name)).setText(listName);
        ((TextView) findViewById(R.id.list_item_local_currency)).setText(localCurrency);

        SharedPreferences preferences = getSharedPreferences(getApplication().getString(R.string.preferences_file), Context.MODE_PRIVATE);
        String defaultCurrencyCode = preferences.getString(getApplication().getString(R.string.preferences_default_currency), "USD");
        double rate = getRate();
        ((TextView) findViewById(R.id.list_item_rate)).setText(
                getApplication().getString(R.string.rate_format,
                        String.format(Locale.US, "%.2f", rate), localCurrency,
                        defaultCurrencyCode));

        SQLiteDatabase db = itemHelper.getReadableDatabase();

        String table = getResources().getString(R.string.list_table);
        String[] listColumns = getResources().getStringArray(R.array.list_columns);
        Cursor cursor = db.rawQuery("SELECT * FROM " + table + " WHERE " + listColumns[0] + "='" + listName + "'", null);
        cursor.moveToNext();
        Double total = cursor.getDouble(cursor.getColumnIndex(listColumns[3]));
        cursor.close();
        String defaultCurrency = defaultCurrencyCode;
        if (defaultCurrency.equals("USD")) {
            defaultCurrency = "$";
        }
        ((TextView) findViewById(R.id.list_item_default_total)).setText(
                getApplication().getString(R.string.total_format,
                        String.format(Locale.US, "%.2f", total / rate),
                        defaultCurrency));
    }

    /**
     * Sets up item adapter for the recycler view
     * @return The item adapter for the recycler view
     */
    private ItemAdapter getItemAdapter() {
        List<ItemEntry> items = getItems();

        SharedPreferences preferences = getSharedPreferences(getApplication().getString(
                R.string.preferences_file), Context.MODE_PRIVATE);
        String defaultCurrencyCode = preferences.getString(getApplication().getString(
                R.string.preferences_default_currency), "USD");

        return new ItemAdapter(this, items, listName, localCurrency,
                defaultCurrencyCode, getRate(), (TextView) findViewById(R.id.list_item_default_total));
    }

    /**
     * Returns the conversion rate from the default currency to localCurrency
     * @return The conversion rate
     */
    private double getRate() {
        SQLiteDatabase db = itemHelper.getReadableDatabase();
        String table = getResources().getString(R.string.rates_table);
        String[] ratesColumns = getResources().getStringArray(R.array.rates_columns);
        Cursor cursor = db.rawQuery("SELECT * FROM " + table + " WHERE " + ratesColumns[0] + "='" + localCurrency + "'", null);
        cursor.moveToNext();
        Double rate = cursor.getDouble(cursor.getColumnIndex(ratesColumns[1]));
        cursor.close();
        return rate;
    }


    /**
     * Get list of items that are a part of this list, to be displayed in recyclerview
     * @return list of items for list
     */
    private List<ItemEntry> getItems() {
        List<ItemEntry> items = new ArrayList<>();
        Cursor cursor = itemHelper.getTable(listName);
        String[] itemColumns = getResources().getStringArray(R.array.items_columns);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(itemColumns[0]));
            String description = cursor.getString(cursor.getColumnIndex(itemColumns[1]));
            double localCost = cursor.getDouble(cursor.getColumnIndex(itemColumns[2]));
            String dateAdded = cursor.getString(cursor.getColumnIndex(itemColumns[3]));
            double latitude = cursor.getDouble(cursor.getColumnIndex(itemColumns[4]));
            double longitude = cursor.getDouble(cursor.getColumnIndex(itemColumns[5]));
            String type = cursor.getString(cursor.getColumnIndex(itemColumns[6]));
            ItemEntry entry = new ItemEntry(name, description, localCost, dateAdded, latitude, longitude, type);
            items.add(entry);
        }
        return items;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_test) {
            Toast.makeText(getApplicationContext(), GetLocalCurrency.getLocalCurrency(this), Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class CreateItemListener implements View.OnClickListener, MenuItem.OnMenuItemClickListener {
        private Dialog dialog;

        CreateItemListener(Dialog dialog) { this.dialog = dialog; }

        @Override
        public void onClick(View v) {
            saveInfo();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            saveInfo();
            return true;
        }

        private void saveInfo() {
            String name = ((EditText) dialog.findViewById(R.id.edit_item_name)).getText().toString();
            if (name.length() > 0) {
                // Have actual item with name
                String description = ((EditText) dialog.findViewById(R.id.edit_item_description)).getText().toString();
                String dateCreated = Calendar.getInstance().getTime().toString();
                String costText = ((EditText) dialog.findViewById(R.id.edit_item_cost)).getText().toString();
                double cost = 0;
                if (costText.length() != 0) {
                    cost =  Double.parseDouble(costText);
                }
                boolean useLocation = ((CheckBox) dialog.findViewById(R.id.location_check_box)).isChecked();
                double latitude = getResources().getInteger(R.integer.INVALID_COORDINATE);
                double longitude = getResources().getInteger(R.integer.INVALID_COORDINATE);
                if (useLocation && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Indicated to use location and can use location
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location lastLocation = null;
                    if (locationManager != null) {
                        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    if (lastLocation != null) {
                        latitude = lastLocation.getLatitude();
                        longitude = lastLocation.getLongitude();
                    }
                }
                String type = ((Spinner) dialog.findViewById(R.id.spinner_purchase_type)).getSelectedItem().toString();
                String[] itemColumns = getResources().getStringArray(R.array.items_columns);
                ContentValues contentValues = new ContentValues();
                contentValues.put(itemColumns[0], name);
                contentValues.put(itemColumns[1], description);
                contentValues.put(itemColumns[2], cost);
                contentValues.put(itemColumns[3], dateCreated);
                contentValues.put(itemColumns[4], latitude);
                contentValues.put(itemColumns[5], longitude);
                contentValues.put(itemColumns[6], type);
                itemHelper.insertRecord(contentValues);
                updateListInfo(addToTotal(cost));
                ItemEntry entry = new ItemEntry(name, description, cost, dateCreated, latitude, longitude, type);
                ((ItemAdapter) recyclerView.getAdapter()).addEntry(entry);
                dialog.dismiss();
                updateModified(dateCreated);
            } else {
                // Reprompt for information

            }
        }
    }

    /**
     * Adds the given cost to the current lists total price
     * @param cost The cost to be added
     * @return The new total for the current list
     */
    private double addToTotal(double cost) {
        String listTable = getResources().getString(R.string.list_table);
        String[] listColumns = getResources().getStringArray(R.array.list_columns);
        String[] listTypes = getResources().getStringArray(R.array.list_types);
        SQLiteHelper totalHelper = new SQLiteHelper(getApplicationContext(), listTable, listColumns, listTypes);

        SQLiteDatabase db = totalHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + listTable + " WHERE " + listColumns[0] + "='" + listName + "'", null);
        cursor.moveToNext();
        double total = cursor.getDouble(cursor.getColumnIndex(listColumns[3]));
        cursor.close();
        total += cost;
        totalHelper.updateRecord(0, listName, 3, Double.toString(total));
        return total;
    }

    /**
     * Updates the list table for the most recently modified time
     * @param modified
     */
    private void updateModified(String modified) {
        String listTable = getResources().getString(R.string.list_table);
        String[] listColumns = getResources().getStringArray(R.array.list_columns);
        String[] listTypes = getResources().getStringArray(R.array.list_types);
        SQLiteHelper modifyHelper = new SQLiteHelper(getApplicationContext(), listTable, listColumns, listTypes);
        modifyHelper.updateRecord(0, listName, 4, modified);
    }

    /**
     * Updates the total cost of the list
     *
     * @param total The new total for the list
     */
    private void updateListInfo(double total) {
        SharedPreferences preferences = getSharedPreferences(getApplication().getString(R.string.preferences_file), Context.MODE_PRIVATE);
        String defaultCurrencyCode = preferences.getString(getApplication().getString(R.string.preferences_default_currency), "USD");
        ((TextView) findViewById(R.id.list_item_default_total)).setText(getApplication().getResources().getString(
                R.string.total_format, String.format(Locale.US, "%.2f", total / getRate()), defaultCurrencyCode));
    }
}
