package com.example.riley.currencyconverter.MainActivity;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.riley.currencyconverter.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Constructs a dialog for list creation
 */
public class CreateListDialogFragment extends DialogFragment {
    public static final String CREATE_TAG = "CREATE_LIST_DIALOG";
    public static final String UPDATE_TAG = "UPDATE_LIST_DIALOG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.FullDialog);

        View view = inflater.inflate(R.layout.activity_main_create_list_dialog, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        setButtonClose((ImageButton) toolbar.findViewById(R.id.toolbar_close));
        toolbar.inflateMenu(R.menu.menu_full_list_info);


        Spinner spinner = view.findViewById(R.id.spinner_create_list_currencies);
        String[] currencies = getResources().getStringArray(R.array.currency_names_full);
        reorderCurrencies(currencies);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_currency, currencies);
        spinner.setAdapter(spinnerAdapter);

        return view;
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
                System.err.println(localCurrency);
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
