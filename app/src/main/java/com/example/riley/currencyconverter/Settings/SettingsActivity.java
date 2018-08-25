package com.example.riley.currencyconverter.Settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.riley.currencyconverter.MainActivity.GetLocalCurrency;
import com.example.riley.currencyconverter.R;
import com.example.riley.currencyconverter.UpdateRates.UpdateTask;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.settings_activity);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        } else if (id == R.id.action_test) {
            System.err.println(GetLocalCurrency.getLocalCurrency(this));
            return true;
        } else if (id == R.id.action_update_rates) {
            update();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the rates table, or alerts user of Internet difficulties
     */
    private void update() {
        new UpdateTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
