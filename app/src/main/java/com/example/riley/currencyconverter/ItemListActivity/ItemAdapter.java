package com.example.riley.currencyconverter.ItemListActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.riley.currencyconverter.LocalStorage.SQLiteHelper;
import com.example.riley.currencyconverter.R;

import java.util.Iterator;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private Context context;
    private List<ItemEntry> entries;
    private SQLiteHelper sqLiteHelper;
    private String localCurrencyCode;
    private String defaultCurrencyCode;
    private double rate;

    ItemAdapter(Context context, List<ItemEntry> entries, String table, String localCurrencyCode, String defaultCurrencyCode, double rate) {
        this.context = context;
        this.entries = entries;
        this.localCurrencyCode = localCurrencyCode;
        this.defaultCurrencyCode = defaultCurrencyCode;
        this.rate = rate;
        String[] columns = context.getResources().getStringArray(R.array.items_columns);
        String[] types = context.getResources().getStringArray(R.array.items_types);
        this.sqLiteHelper = new SQLiteHelper(context, table, columns, types);
    }

    @NonNull
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_list_container, parent, false);
        return new ItemAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemAdapter.ItemViewHolder holder, int position) {
        final ItemEntry entry = entries.get(position);
        setTextViews(holder, entry);

        // Set the listener for opening up the list of items activity for the item that
        // was clicked on
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            Iterator<ItemEntry> iter = entries.iterator();
                            int index = 0;
                            while (iter.hasNext()) {
                                ItemEntry curr = iter.next();
                                if (curr.equals(entry)) {
                                    iter.remove();
                                    notifyItemRemoved(index);
                                    sqLiteHelper.removeRecord(entry.getName());
                                }
                                index++;
                            }
                            System.err.println("Delete");
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
        holder.name.setText(entry.getName());
        holder.description.setText(entry.getDescription());
        holder.localCost.setText(String.format("%.2f", entry.getCost()) + " " + localCurrencyCode);
        holder.defaultCost.setText(String.format("%.2f", entry.getCost() * rate) + " " + defaultCurrencyCode);
        holder.dateAdded.setText(entry.getCreated());
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

        private ItemViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.name = itemView.findViewById(R.id.item_name);
            this.description = itemView.findViewById(R.id.item_description);
            this.localCost = itemView.findViewById(R.id.item_local_cost);
            this.defaultCost = itemView.findViewById(R.id.item_default_cost);
            this.dateAdded = itemView.findViewById(R.id.item_date_added);
        }
    }
}
