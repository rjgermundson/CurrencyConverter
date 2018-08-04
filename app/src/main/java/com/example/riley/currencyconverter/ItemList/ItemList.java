package com.example.riley.currencyconverter.ItemList;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.riley.currencyconverter.R;

/**
 * This class acts as the individual item list activity
 * The user should be able to add and remove items from their list
 */
public class ItemList extends AppCompatActivity {

    private String database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_list);
    }

}
