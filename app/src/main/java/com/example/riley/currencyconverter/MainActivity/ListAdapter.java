package com.example.riley.currencyconverter.MainActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.riley.currencyconverter.ItemListActivity.ItemListActivity;
import com.example.riley.currencyconverter.LocalStorage.SQLiteHelper;
import com.example.riley.currencyconverter.R;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private Context context;
    private List<ListEntry> entries;
    private SQLiteHelper sqLiteHelper;

    ListAdapter(Context context, List<ListEntry> entries) {
        this.context = context;
        this.entries = entries;
        String table = context.getString(R.string.list_table);
        String[] columns = context.getResources().getStringArray(R.array.list_columns);
        String[] types = context.getResources().getStringArray(R.array.list_types);
        this.sqLiteHelper = new SQLiteHelper(context, table, columns, types);
    }

    @NonNull
    @Override
    public ListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_main_list_container, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListViewHolder holder, int position) {
        final ListEntry entry = entries.get(position);
        setTextViews(holder, entry);

        // Set the listener for opening up the list of items activity for the item that
        // was clicked on
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(), ItemListActivity.class);
                intent.putExtra(context.getApplicationContext().getString(R.string.list_name), entry.getName());
                intent.putExtra(context.getApplicationContext().getString(R.string.local_currency), entry.getLocalCurrency());
                context.startActivity(intent);
            }
        });


        // Set up long click listener that will open up a dialog allowing for deleting or
        // editing a record
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                final CharSequence[] options = {"Update", "Delete"};
                alertBuilder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        if (choice == 0) {  // Update
                            System.err.println("Update");
                        } else if (choice == 1) {  // Delete
                            Iterator<ListEntry> iter = entries.iterator();
                            int index = 0;
                            while (iter.hasNext()) {
                                ListEntry curr = iter.next();
                                if (curr.equals(entry)) {
                                    iter.remove();
                                    notifyItemRemoved(index);
                                    sqLiteHelper.removeRecord(entry.getName());
                                    sqLiteHelper.dropTable(entry.getName());
                                }
                                index++;
                            }
                            System.err.println("Delete");
                        }
                    }
                });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();
                return true;
            }
        });
    }

    private void setTextViews(ListViewHolder holder, ListEntry entry) {
        // Look up rate for the given entry from the stored rates
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        String table = context.getResources().getString(R.string.rates_table);
        String[] ratesColumns = context.getResources().getStringArray(R.array.rates_columns);
        Cursor cursor = db.rawQuery("SELECT * FROM " + table + " WHERE " + ratesColumns[0] + "='" + entry.getLocalCurrency() + "'", null);
        cursor.moveToNext();
        Double rate = cursor.getDouble(cursor.getColumnIndex(ratesColumns[1]));
        cursor.close();

        holder.name.setText(entry.getName());
        holder.description.setText(entry.getDescription());
        holder.localCurrency.setText(entry.getLocalCurrency());
        holder.total.setText(context.getResources().getString(
                R.string.total_format,
                String.format(Locale.US, "%.2f", entry.getTotal() / rate),
                entry.getDefaultCurrency()));
        holder.modified.setText(entry.getModified());
        holder.rate.setText(context.getResources().getString(
                R.string.rate_format,
                String.format(Locale.US, "%.2f", rate),
                entry.getDefaultCurrency()));
    }

    @Override
    public int getItemCount() {
        if (entries != null) {
            return entries.size();
        }
        return 0;
    }

    public void addEntry(ListEntry entry) {
        entries.add(entry);
        notifyItemInserted(entries.size());
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView name;
        TextView description;
        TextView localCurrency;
        TextView rate;
        TextView total;
        TextView modified;

        private ListViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.name = itemView.findViewById(R.id.list_item_name);
            this.description = itemView.findViewById(R.id.list_item_description);
            this.localCurrency = itemView.findViewById(R.id.list_item_local_currency);
            this.rate = itemView.findViewById(R.id.list_item_rate);
            this.total = itemView.findViewById(R.id.list_item_default_total);
            this.modified = itemView.findViewById(R.id.list_item_date_touched);
        }
    }
}

