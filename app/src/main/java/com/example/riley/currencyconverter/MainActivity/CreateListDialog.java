package com.example.riley.currencyconverter.MainActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.riley.currencyconverter.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Constructs a dialog for list creation
 */
public class CreateListDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_main_create_list_dialog, null);
        Spinner spinner = view.findViewById(R.id.create_list_spinner_currencies);
        String[] currencies = getResources().getStringArray(R.array.currency_names_full);
        reorderCurrencies(currencies);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.currency_spinner, currencies);
        spinner.setAdapter(spinnerAdapter);
        builder.setView(view);
        return builder.create();
    }

    /**
     * Reorders the currencies array to begin with the local currency as determined by user's GPS
     * location
     *
     * @param currencies List of full names of all supported currencies
     */
    private void reorderCurrencies(String[] currencies) {
        List<String> toSort = new ArrayList<>();
        Collections.addAll(toSort, currencies);
        Collections.sort(toSort);
        int index = 0;
        for (String str : toSort) {
            currencies[index] = str;
            index++;
        }
        String localCurrency = GetLocalCurrency.getLocalCurrency(getActivity());
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(localCurrency)) {
                // Have index of full name that needs to be swapped to index 0
                // and everything else moved over until
                for (int j = i; j > 0; j--) {
                    currencies[j] = currencies[j - 1];
                }
                currencies[0] = localCurrency;
                break;
            }
        }
    }
}
