package com.example.riley.currencyconverter.ItemListActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.example.riley.currencyconverter.R;

public class CreateItemDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.item_list_create_list_dialog, null);
        // Check if GPS is enabled to determine whether the dialog needs the use location
        // checkbox
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            view.findViewById(R.id.location_check_box).setVisibility(View.GONE);
        } else {
            // Set use location as true for default when access to location is granted
            ((CheckBox) view.findViewById(R.id.location_check_box)).setChecked(true);
        }
        builder.setView(view);
        return builder.create();
    }

}
