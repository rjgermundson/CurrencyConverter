package com.example.riley.currencyconverter.ItemListActivity;

import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.riley.currencyconverter.R;

public class CreateItemDialogFragment extends DialogFragment {
    public static final String TAG = "ITEM_DIALOG";
    private static String localCurrency;

    /**
     * Constructor for CreateItemDialog object
     * @throws UnsupportedOperationException if called before newInstance()
     */
    public CreateItemDialogFragment() {
        if (localCurrency == null) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialog);
    }

    /**
     * Sets the localCurrency for CreateItemDialogs
     *
     * @param currency Local currency to be used for CreateItemDialogs
     * @return A CreateItemDialog object
     */
    public static CreateItemDialogFragment getInstance(String currency) {
        localCurrency = currency;
        return new CreateItemDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.FullDialog);

        final View view = inflater.inflate(R.layout.activity_item_list_create_list_dialog, container);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        setButtonClose((ImageButton) toolbar.findViewById(R.id.toolbar_close));
        toolbar.inflateMenu(R.menu.menu_full_list_info);
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
        ((TextView) view.findViewById(R.id.item_local_currency)).setText(localCurrency);

        Spinner spinner = view.findViewById(R.id.spinner_purchase_type);
        String[] purchases = getResources().getStringArray(R.array.purchase_types);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_purchase_type, purchases);
        spinner.setAdapter(spinnerAdapter);

        return view;
    }

    /**
     * Sets the given button to close this dialog
     * @param button Button to set
     */
    private void setButtonClose(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }
}