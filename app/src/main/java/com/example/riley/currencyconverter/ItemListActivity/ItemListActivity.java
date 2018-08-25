package com.example.riley.currencyconverter.ItemListActivity;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.riley.currencyconverter.MainActivity.GetLocalCurrency;
import com.example.riley.currencyconverter.R;
import com.example.riley.currencyconverter.Settings.SettingsActivity;

public class ItemListActivity extends AppCompatActivity {
    private String listName;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.item_list_activity);
        listName = getIntent().getExtras().getString(getApplicationContext().getString(R.string.list_name));
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment alert = new CreateItemDialog();
                alert.show(getFragmentManager(), "create");
                getFragmentManager().beginTransaction().commit();
                getFragmentManager().executePendingTransactions();
                Button save = alert.getDialog().findViewById(R.id.create_item_button);
                save.setOnClickListener(new CreateItemListener(alert.getDialog()));
            }
        });
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

    private class CreateItemListener implements View.OnClickListener {
        private Dialog dialog;

        CreateItemListener(Dialog dialog) { this.dialog = dialog; }

        @Override
        public void onClick(View v) {
            String name = ((EditText) dialog.findViewById(R.id.edit_item_name)).getText().toString();
            if (name.length() > 0) {
                // Have actual item with name

            } else {

            }
        }
    }
}
