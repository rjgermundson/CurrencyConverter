package com.example.riley.currencyconverter.MainActivity;

import android.content.Context;

import com.example.riley.currencyconverter.R;

/**
 * This class contains two utility methods for translating between currency codes and full
 * currency names
 */
public class CurrencyTypeConverter {

    /**
     * Translates the given currency code to its full name
     * @param context Context for activity
     * @param abbrev Abbreviation to translate
     * @return The full name for the given abbreviation
     */
    public static String abbrevToFull(Context context, String abbrev) {
        String result = "";
        String[] abbrevs = context.getResources().getStringArray(R.array.currency_names_abbrev);
        String[] full = context.getResources().getStringArray(R.array.currency_names_full);
        for (int i = 0; i < abbrevs.length; i++) {
            if (abbrevs[i].equals(abbrev)) {
                return full[i];
            }
        }
        return result;
    }

    /**
     * Translates the given full name to its currency code
     * @param context The activity context
     * @param currency The full currency name
     * @return The translation of the full currency name
     */
    public static String fullToAbbrev(Context context, String currency) {
        String result = "";
        String[] abbrevs = context.getResources().getStringArray(R.array.currency_names_abbrev);
        String[] full = context.getResources().getStringArray(R.array.currency_names_full);
        for (int i = 0; i < abbrevs.length; i++) {
            if (full[i].equals(currency)) {
                return abbrevs[i];
            }
        }
        return result;
    }
}
