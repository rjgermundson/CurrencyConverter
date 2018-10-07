package com.example.riley.currencyconverter.ItemListActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.riley.currencyconverter.FullItemInfo.FullItemInfoActivity;
import com.example.riley.currencyconverter.LocalStorage.SQLiteHelper;
import com.example.riley.currencyconverter.R;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private Activity activity;
    private List<ItemEntry> entries;
    private SQLiteHelper sqLiteHelper;
    private String table;
    private String localCurrencyCode;
    private String defaultCurrencyCode;
    private double rate;
    private TextView listInfo;

    ItemAdapter(Activity activity, List<ItemEntry> entries, String table, String localCurrencyCode,
                String defaultCurrencyCode, double rate, TextView listInfo) {
        this.activity = activity;
        this.entries = entries;
        this.table = table;
        this.localCurrencyCode = localCurrencyCode;
        this.defaultCurrencyCode = defaultCurrencyCode;
        this.rate = rate;
        this.listInfo = listInfo;
        String[] columns = activity.getResources().getStringArray(R.array.items_columns);
        String[] types = activity.getResources().getStringArray(R.array.items_types);
        this.sqLiteHelper = new SQLiteHelper(activity, table, columns, types);
    }

    @NonNull
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_item_list_container, parent, false);
        return new ItemAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemAdapter.ItemViewHolder holder, int position) {
        final ItemEntry entry = entries.get(position);
        setTextViews(holder, entry);

        // Set the listener for opening up the item's full info activity for the item that
        // was clicked on
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, FullItemInfoActivity.class);
                intent.putExtra(activity.getResources().getString(R.string.local_currency), localCurrencyCode);
                intent.putExtra(activity.getResources().getString(R.string.item_rate), rate);
                String[] info = {entry.getName(), entry.getDescription(), Double.toString(entry.getCost()),
                                 entry.getCreated(), Double.toString(entry.getLatitude()),
                                 Double.toString(entry.getLongitude()), entry.getType(),
                                 entry.getKey()};
                intent.putExtra(activity.getResources().getString(R.string.item_info), info);
                activity.startActivity(intent);
            }
        });
        // Set up long click listener that will open up a dialog allowing for deleting or
        // editing a record
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                final CharSequence[] options = {"Update", "Delete"};
                alertBuilder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        if (choice == 0) {  // Update

                            FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
                            DialogFragment prev = (DialogFragment) activity.getFragmentManager().findFragmentByTag("dialog");
                            if (prev != null) {
                                fragmentTransaction.remove(prev);
                            }
                            DialogFragment fragment = CreateItemDialogFragment.getInstance(localCurrencyCode);
                            fragment.show(fragmentTransaction, CreateItemDialogFragment.TAG);
                            activity.getFragmentManager().executePendingTransactions();

                            // Set views
                            Dialog fragmentDialog = fragment.getDialog();
                            ((EditText) fragmentDialog.findViewById(R.id.edit_item_name)).setText(entry.getName());
                            ((EditText) fragmentDialog.findViewById(R.id.edit_item_description)).setText(entry.getDescription());
                            ((EditText) fragmentDialog.findViewById(R.id.edit_item_cost)).setText(String.format(Locale.US, "%.2f", entry.getCost()));
                            String[] purchaseTypes = activity.getResources().getStringArray(R.array.purchase_types);
                            int index;
                            for (index = 0; index < purchaseTypes.length; index++) {
                                if (purchaseTypes[index].equals(entry.getType())) {
                                    break;
                                }
                            }
                            ((Spinner) fragmentDialog.findViewById(R.id.spinner_purchase_type)).setSelection(index);

                            ((Toolbar) fragment.getDialog().findViewById(R.id.toolbar)).getMenu()
                                    .getItem(0).setOnMenuItemClickListener(
                                    new UpdateItemListener(fragment.getDialog(), entry));
                            Button save = fragment.getDialog().findViewById(R.id.create_item_button);
                            save.setOnClickListener(new UpdateItemListener(fragment.getDialog(), entry));
                        } else if (choice == 1) {  // Delete
                            Iterator<ItemEntry> iter = entries.iterator();
                            int index = 0;
                            while (iter.hasNext()) {
                                ItemEntry curr = iter.next();
                                if (curr.equals(entry)) {
                                    iter.remove();
                                    notifyItemRemoved(index);
                                    sqLiteHelper.removeRecord(entry.getName());
                                    updateModified(Calendar.getInstance().getTime().toString());
                                }
                                index++;
                            }
                            updateListInfo(removeFromTotal(-entry.getCost()));
                        }
                    }
                });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();
                System.err.println("LONG CLICK");
                return true;
            }
        });
    }

    private void setTextViews(ItemAdapter.ItemViewHolder holder, ItemEntry entry) {
        String localCurrency = localCurrencyCode;
        String defaultCurrency = defaultCurrencyCode;
        if (localCurrency.equals("USD")) {
            localCurrency = "$";
        }
        if (defaultCurrency.equals("USD")) {
            defaultCurrency = "$";
        }
        holder.name.setText(entry.getName());
        holder.description.setText(entry.getDescription());
        holder.localCost.setText(activity.getResources().getString(
                R.string.total_format,
                String.format(Locale.US, "%.2f", entry.getCost()),
                localCurrency));
        holder.defaultCost.setText(activity.getResources().getString(R.string.total_format,
                String.format(Locale.US, "%.2f", entry.getCost() / rate),
                defaultCurrency));
        holder.dateAdded.setText(entry.getCreated());

        String type = entry.getType();
        ImageView typeImage = holder.typeImage;
        switch (type) {
            case "Entertainment":
                typeImage.setImageResource(R.drawable.ic_local_activity_black_36dp);
                break;
            case "Food":
                typeImage.setImageResource(R.drawable.ic_local_dining_black_36dp);
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
    public int getItemCount() {
        if (entries != null) {
            return entries.size();
        }
        return 0;
    }

    public void addEntry(ItemEntry entry) {
        entries.add(entry);
        notifyItemInserted(entries.size());
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView name;
        TextView description;
        TextView localCost;
        TextView defaultCost;
        TextView dateAdded;
        ImageView typeImage;

        private ItemViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.name = itemView.findViewById(R.id.item_name);
            this.description = itemView.findViewById(R.id.item_description);
            this.localCost = itemView.findViewById(R.id.item_local_cost);
            this.defaultCost = itemView.findViewById(R.id.item_default_cost);
            this.dateAdded = itemView.findViewById(R.id.item_date_added);
            this.typeImage = itemView.findViewById(R.id.item_type_image);
        }
    }

    /**
     * Subtracts the given cost from the current lists total price
     * @param cost The cost to be added
     * @return The new total for the current list
     */
    private double removeFromTotal(double cost) {
        String listTable = activity.getResources().getString(R.string.list_table);
        String[] listColumns = activity.getResources().getStringArray(R.array.list_columns);
        String[] listTypes = activity.getResources().getStringArray(R.array.list_types);
        SQLiteHelper totalHelper = new SQLiteHelper(activity, listTable, listColumns, listTypes);

        SQLiteDatabase db = totalHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + listTable + " WHERE " + listColumns[0] + "='" + table + "'", null);
        cursor.moveToNext();
        double total = cursor.getDouble(cursor.getColumnIndex(listColumns[3]));
        cursor.close();
        total += cost;
        totalHelper.updateRecord(0, table, 3, Double.toString(total));
        return total;
    }

    /**
     * Updates the list table for the most recently modified time
     * @param modified New modified time stamp
     */
    private void updateModified(String modified) {
        String listTable = activity.getResources().getString(R.string.list_table);
        String[] listColumns = activity.getResources().getStringArray(R.array.list_columns);
        String[] listTypes = activity.getResources().getStringArray(R.array.list_types);
        SQLiteHelper modifyHelper = new SQLiteHelper(activity, listTable, listColumns, listTypes);
        modifyHelper.updateRecord(0, table, 4, modified);
    }

    /**
     * Updates the total cost of the list
     *
     * @param total The new total for the list
     */
    private void updateListInfo(double total) {
        String defaultCurrency = defaultCurrencyCode;
        System.err.println(defaultCurrencyCode);
        if (defaultCurrency.equals("USD")) {
            defaultCurrency = "$";
        }
        ((TextView) listInfo.findViewById(R.id.list_item_default_total)).setText(activity.getResources().getString(R.string.total_format,
                String.format(Locale.US, "%.2f", total / rate),
                defaultCurrency));
    }

    private class UpdateItemListener implements View.OnClickListener, MenuItem.OnMenuItemClickListener {
        Dialog dialog;
        ItemEntry entry;

        private UpdateItemListener(Dialog dialog, ItemEntry entry) {
            this.dialog = dialog;
            this.entry = entry;
        }

        @Override
        public void onClick(View v) {
            updateInfo();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            updateInfo();
            return true;
        }

        private void updateInfo() {
            EditText nameText = dialog.findViewById(R.id.edit_item_name);
            EditText descriptionText = dialog.findViewById(R.id.edit_item_description);
            EditText costText = dialog.findViewById(R.id.edit_item_cost);
            Spinner typeSpinner = dialog.findViewById(R.id.spinner_purchase_type);
            boolean useLocation = ((CheckBox) dialog.findViewById(R.id.location_check_box)).isChecked();

            String name = nameText.getText().toString();
            String description = descriptionText.getText().toString();
            double cost = Double.parseDouble(costText.getText().toString());
            String type = typeSpinner.getSelectedItem().toString();
            double latitude = activity.getResources().getInteger(R.integer.INVALID_COORDINATE);
            double longitude = activity.getResources().getInteger(R.integer.INVALID_COORDINATE);
            if (useLocation && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Indicated to use location and can use location
                LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
                Location lastLocation = null;
                if (locationManager != null) {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (lastLocation != null) {
                    latitude = lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();
                }
            }
            ContentValues values = new ContentValues();
            String[] itemColumns = activity.getResources().getStringArray(R.array.items_columns);
            values.put(itemColumns[0], name);
            values.put(itemColumns[1], description);
            values.put(itemColumns[2], cost);
            values.put(itemColumns[4], latitude);
            values.put(itemColumns[5], longitude);
            values.put(itemColumns[6], type);
            sqLiteHelper.updateRecord(7, entry.getKey(), values);

            // Update modified
            String modified = Calendar.getInstance().getTime().toString();
            updateModified(modified);
            // Update list total
            updateListInfo(removeFromTotal(cost - entry.getCost()));

            entry.setName(name);
            entry.setDescription(description);
            entry.setCost(cost);
            entry.setLatitude(latitude);
            entry.setLongitude(longitude);
            entry.setType(type);
            notifyDataSetChanged();
            dialog.dismiss();
        }
    }
}
