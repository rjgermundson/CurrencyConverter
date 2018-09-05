package com.example.riley.currencyconverter.MainActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.riley.currencyconverter.LocalStorage.SQLiteHelper;
import com.example.riley.currencyconverter.R;
import com.example.riley.currencyconverter.Settings.SettingsActivity;
import com.example.riley.currencyconverter.UpdateRates.UpdateTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    SQLiteHelper listHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initial Setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.recyclerView = findViewById(R.id.list_recycler_view);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        // Check permission for GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Neither permission granted
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    R.integer.PERMISSION_FINE_LOCATION_REQUEST_CODE);
        }
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        // Setup button for adding a new list
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                DialogFragment prev = (DialogFragment) getFragmentManager().findFragmentByTag(CreateListDialogFragment.TAG);
                if (prev != null) {
                    fragmentTransaction.remove(prev);
                }
                DialogFragment dialogFragment = new CreateListDialogFragment();
                dialogFragment.show(fragmentTransaction, CreateListDialogFragment.TAG);
                getFragmentManager().executePendingTransactions();
                ((Toolbar) dialogFragment.getDialog().findViewById(R.id.toolbar)).getMenu()
                        .getItem(0).setOnMenuItemClickListener(
                        new CreateListListener(dialogFragment.getDialog()));
                Button save = dialogFragment.getDialog().findViewById(R.id.create_list_button);
                save.setOnClickListener(new CreateListListener(dialogFragment.getDialog()));
            }
        });

        // Check whether rates have been imported, if not import them
        String table = getApplication().getString(R.string.list_table);
        String[] columns = getApplication().getResources().getStringArray(R.array.list_columns);
        String[] types = getApplication().getResources().getStringArray(R.array.list_types);
        listHelper = new SQLiteHelper(getApplicationContext(), table, columns, types);
        if (!listHelper.tableExists(getString(R.string.rates_table))) {
            update();
            System.out.println("IMPORT");
        }

        // Check preferences for whether default currency has been setup
        SharedPreferences preferences = this.getSharedPreferences(getApplication().getString(R.string.preferences_file),
                Context.MODE_PRIVATE);
        if (preferences.getAll().size() == 0) {
            // Set default preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getApplication().getString(R.string.preferences_default_currency),
                    "USD");
            editor.apply();
        }

        // Create list of lists
        getLists();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new ListAdapter(this, getLists()));
    }

    /**
     * Clear any DialogFragments from the given FragmentManager and all of the
     * child fragments
     */
    private void dismissDialogFragments(FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof DialogFragment) {
                    ((DialogFragment) fragment).dismissAllowingStateLoss();
                }
                FragmentManager childManager = fragment.getChildFragmentManager();
                if (childManager != null) {
                    dismissDialogFragments(childManager);
                }
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        recyclerView.setAdapter(new ListAdapter(this, getLists()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            System.err.println(GetLocalCurrency.getLocalCurrency(this));
            return true;
        } else if (id == R.id.action_update_rates) {
            update();
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the rates table, or alerts user of Internet difficulties
     */
    private void update() {
        new UpdateTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Returns a list with the lists that are in the database
     */
    private List<ListEntry> getLists() {
        ArrayList<ListEntry> entries = new ArrayList<>();
        String listTable = getApplication().getString(R.string.list_table);
        String[] listColumns = getApplication().getResources().getStringArray(R.array.list_columns);

        Cursor cursor = listHelper.getTable(listTable);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(listColumns[0]));
            String description = cursor.getString(cursor.getColumnIndex(listColumns[1]));
            String localCurrency = cursor.getString(cursor.getColumnIndex(listColumns[2]));
            SharedPreferences preferences = getSharedPreferences(getApplication().getString(R.string.preferences_file), Context.MODE_PRIVATE);
            String defaultCurrency = preferences.getString(getApplication().getString(R.string.preferences_default_currency), "USD");
            Double total = cursor.getDouble(cursor.getColumnIndex(listColumns[3]));
            String modified = cursor.getString(cursor.getColumnIndex(listColumns[4]));
            String created = cursor.getString(cursor.getColumnIndex(listColumns[5]));
            ListEntry entry = new ListEntry(name, description, localCurrency, defaultCurrency, total, modified, created);
            entries.add(entry);
        }
        return entries;
    }

    /**
     * This class serves as the save listener that will create a new list
     * when the user specifies a list name
     */
    private class CreateListListener implements View.OnClickListener, MenuItem.OnMenuItemClickListener {
        Dialog dialog;

        /**
         * Constructor for save button
         * @param dialog Dialog the save button and name or description
         *               fields occupy
         */
        CreateListListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View view) {
            saveInfo();
        }

        /**
         * Searches the list table for whether a list of the given name exists
         */
        private boolean checkListExists(String name) {
            if (name == null) {
                return false;
            }
            String listTable = getApplication().getString(R.string.list_table);
            String[] listColumns = getApplication().getResources().getStringArray(R.array.list_columns);
            Cursor cursor = listHelper.getTable(listTable);
            while (cursor.moveToNext()) {
                if (name.equals(cursor.getString(cursor.getColumnIndex(listColumns[0])))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            saveInfo();
            return true;
        }

        private void saveInfo() {
            final EditText nameText = dialog.findViewById(R.id.edit_list_name);
            EditText descriptionText = dialog.findViewById(R.id.edit_description);
            String[] listColumns = getApplication().getResources().getStringArray(R.array.list_columns);
            nameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nameText.setHint("");
                }
            });

            // Get table name and possible description from text fields
            String name = nameText.getText().toString();
            String description = descriptionText.getText().toString();
            if (name.isEmpty() || checkListExists(name)) {
                // No name to create a table for
                nameText.setText("");
                nameText.setHint("Must enter a new list name");
                nameText.setHintTextColor(Color.RED);
            } else {
                SharedPreferences preferences = getSharedPreferences(getApplication().getString(R.string.preferences_file), Context.MODE_PRIVATE);
                String defaultCurrency = preferences.getString(getApplication().getString(R.string.preferences_default_currency), "USD");
                String modified = Calendar.getInstance().getTime().toString();
                ContentValues values = new ContentValues();
                values.put(listColumns[0], name);
                values.put(listColumns[1], description);
                String localCurrency;
                Spinner spinner = dialog.findViewById(R.id.create_list_spinner_currencies);
                localCurrency = spinner.getSelectedItem().toString();
                localCurrency = CurrencyTypeConverter.fullToAbbrev(getApplicationContext(), localCurrency);

                // Construct table for this list's items
                SQLiteHelper itemHelper = new SQLiteHelper(getApplication(), name, getApplication().getResources().getStringArray(R.array.items_columns),
                        getApplication().getResources().getStringArray(R.array.items_columns));
                values.put(listColumns[2], localCurrency);  // Need to get local currency
                values.put(listColumns[3], 0);  // Total
                values.put(listColumns[4], modified);
                values.put(listColumns[5], modified);
                listHelper.insertRecord(values);  // Insert new list into list table
                ((ListAdapter) recyclerView.getAdapter()).addEntry(new ListEntry(name, description, localCurrency, defaultCurrency,0, modified, modified));
                dialog.dismiss();
            }
        }
    }
}
