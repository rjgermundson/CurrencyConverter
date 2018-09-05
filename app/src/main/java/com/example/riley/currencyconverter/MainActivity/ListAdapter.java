package com.example.riley.currencyconverter.MainActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.riley.currencyconverter.ItemListActivity.ItemListActivity;
import com.example.riley.currencyconverter.LocalStorage.SQLiteHelper;
import com.example.riley.currencyconverter.R;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * This class handles the recyclerview functionality for the list in MainActivity
 */
class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private static final long ANIMATION_DURATION = 200;
    private static final int DESCRIPTION_PADDING = 8;
    private Context context;
    private List<ListEntry> entries;  // Entries in list
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
        setViews(holder, entry);

        // Set the listener for opening up the list of items activity for the item that
        // was clicked on
        holder.view.setOnClickListener(new DoubleClickListener(holder, entry.getLocalCurrency()));

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
                            System.err.println("UPDATE");
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
                        }
                    }
                });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();
                return true;
            }
        });
    }

    /**
     * Sets the Views for the given holder using the entry values
     * @param holder Holder to set
     * @param entry Entry to populate holder
     */
    private void setViews(ListViewHolder holder, ListEntry entry) {
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

        if (entry.getDescription().length() == 0) {
            // Don't want to show expand button
            holder.view.findViewById(R.id.list_description_expand).setVisibility(View.GONE);
        }

        String defaultCurrency = entry.getDefaultCurrency();
        if (entry.getDefaultCurrency().equals("USD")) {
            defaultCurrency = "$";
        }
        holder.total.setText(context.getResources().getString(
                R.string.total_format,
                String.format(Locale.US, "%.2f", entry.getTotal() / rate),
                defaultCurrency));
        String modified = formatDate(entry.getModified());
        String created = formatDate(entry.getCreated());
        holder.timeStamps.setText(context.getResources().getString(R.string.modified_created, modified, created));
        holder.rate.setText(context.getResources().getString(
                R.string.rate_format,
                String.format(Locale.US, "%.2f", rate), entry.getLocalCurrency(),
                entry.getDefaultCurrency()));
    }

    /**
     * Formats the given date taken from Calendar format to (month #day, #year)
     * @param date Date to format
     * @return Date in correct format
     */
    private String formatDate(String date) {
        System.out.println(date);
        String[] tokens = date.split(" ");
        String monthDay = tokens[1] + " " + tokens[2];
        String year = tokens[5];
        return context.getResources().getString(R.string.date, monthDay, year);
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

    /**
     * Returns the height in pixels of the given textView
     * @param parent Parent of the given TextView
     * @param textView TextView whose height will be calculated
     * @return Height of the textView in pixels
     */
    private int getTextViewHeight(View parent, TextView textView) {
        parent.measure(0, 0);
        textView.measure(0, 0);

        int parentWidth = parent.getWidth();
        int textWidth = (int) textView.getPaint().measureText(textView.getText().toString());
        int lineHeight = textView.getLineHeight();
        int numberOfLines = (textWidth / parentWidth) + 1;
        if (textWidth != 0) {
            // We have something to expand to
            return numberOfLines * lineHeight + textView.getCompoundPaddingTop() * 2 + DESCRIPTION_PADDING;
        } else {
            // We have nada
            return 0;
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView name;
        TextView description;
        TextView rate;
        TextView total;
        TextView timeStamps;

        private ListViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.name = itemView.findViewById(R.id.list_item_name);
            this.description = itemView.findViewById(R.id.list_item_description);
            this.rate = itemView.findViewById(R.id.list_item_rate);
            this.total = itemView.findViewById(R.id.list_item_default_total);
            this.timeStamps = itemView.findViewById(R.id.list_item_date_touched);
        }
    }

    private class DoubleClickListener implements View.OnClickListener {
        private final long CLICK_TIMEOUT = Double.valueOf(ViewConfiguration.getDoubleTapTimeout() / 1.3).longValue();
        private Handler handler;
        private long lastClick;
        private ListViewHolder holder;
        private String localCurrency;

        /**
         * Constructor for a DoubleClickListener
         * @param holder ListViewHolder using this listener
         */
        private DoubleClickListener(ListViewHolder holder, String localCurrency) {
            this.handler = new Handler(Looper.getMainLooper());
            this.lastClick = 0;
            this.holder = holder;
            this.localCurrency = localCurrency;
        }

        @Override
        public void onClick(View v) {
            long currClickTime = System.currentTimeMillis();
            if (currClickTime - lastClick < CLICK_TIMEOUT) {
                // Clear any single clicks queued and carry out action
                handler.removeCallbacksAndMessages(null);
                lastClick = currClickTime;
                doubleClick();
            } else {
                lastClick = currClickTime;
                // Wait to see if double click occurs
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        singleClick();
                        lastClick = 0;
                    }
                }, CLICK_TIMEOUT);
            }
        }

        private void singleClick() {
                String list = holder.name.getText().toString();
                Intent intent = new Intent(context.getApplicationContext(), ItemListActivity.class);
                intent.putExtra(context.getApplicationContext().getString(R.string.list_name), list);
                intent.putExtra(context.getApplicationContext().getString(R.string.local_currency),localCurrency);
                context.startActivity(intent);
        }

        private void doubleClick() {
            TextView description = holder.description;
            description.measure(0, 0);
            System.err.println("DOUBLEDOUBLE");
            int startHeight = description.getHeight();
            int endHeight;
            if (startHeight == 0) {
                // We want to expand
                endHeight = getTextViewHeight(holder.view, description);
            } else {
                // We want to collapse
                endHeight = 0;
            }
            System.err.println("START: " + startHeight);
            System.err.println("END: " + endHeight);
            description.startAnimation(new RevealAnimation(description, startHeight, endHeight, ANIMATION_DURATION));
            // Change image slightly after/during for aesthetic reasons
            if (startHeight == 0) {
                ((ImageView) holder.view.findViewById(R.id.list_description_expand)).setImageResource(R.drawable.ic_expand_less_black_24dp);
            } else {
                ((ImageView) holder.view.findViewById(R.id.list_description_expand)).setImageResource(R.drawable.ic_expand_more_black_24dp);
            }
            holder.view.requestLayout();
        }
    }

    /**
     * This handles the animation used when revealing an item description
     */
    private class RevealAnimation extends Animation {
        private final View view;
        private final int startHeight;
        private final int endHeight;

        private RevealAnimation(View view, int startHeight, int endHeight, long duration) {
            this.view = view;
            this.startHeight = startHeight;
            this.endHeight = endHeight;
            setDuration(duration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            view.getLayoutParams().height = (int) ((endHeight - startHeight) * interpolatedTime + startHeight);
            view.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}

