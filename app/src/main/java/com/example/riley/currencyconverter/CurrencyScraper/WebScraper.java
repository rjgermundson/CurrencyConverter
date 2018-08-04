package com.example.riley.currencyconverter.CurrencyScraper;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Scanner;

/**
 * The WebScraper will take the currency rates from x-rates.com and create or update
 * a local database for currency abbreviations and currency rates
 *
 * Returns a null table of currencies if connection was interrupted
 */
public class WebScraper extends AsyncTask<String, Void, Hashtable<String, Double>> {

    private static final String X_RATES = "https://x-rates.com/table/?from=USD&amount=1";
    private Hashtable<String, String> abbreviations;
    private String defaultCurrency;

    /**
     * Constructor for WebScraper task
     *
     * @param abbreviations Mapping of full currency names to abbreviated currency names
     * @param defaultCurrency Default currency determined by user
     */
    public WebScraper(Hashtable<String, String> abbreviations, String defaultCurrency) {
        this.abbreviations = abbreviations;
        this.defaultCurrency = defaultCurrency;
    }

    @Override
    protected Hashtable<String, Double> doInBackground(String... arg0) {
        return getCurrencyTable();
    }



    /**
     * Connect to and parse the website for an updated currency
     * table
     * @return A hash table relating currency type to defaultCurrency
     *         null if failed to connect to site, or defaultCurrency not
     *         found
     */
    private Hashtable<String, Double> getCurrencyTable() {
        Scanner scanner;
        if (defaultCurrency.length() != 3) {
            if (abbreviations.contains(defaultCurrency)) {
                defaultCurrency = abbreviations.get(defaultCurrency);
            } else {
                return null;
            }
        }
        try {
            scanner = getScanner();
        } catch (IOException e) {
            return null;
        }

        // Parse HTML for currency rates
        Hashtable<String, Double> rates = new Hashtable<>();
        while (scanner.hasNext()) {
            String currLine = scanner.nextLine();
            if (currLine.contains("<table class=\"tablesorter")){
                while (scanner.hasNextLine()) {
                    String currEntry = scanner.nextLine();
                    if (currEntry.contains("<tr>")) {
                        // Parse table entry for rate
                        currEntry = scanner.nextLine().trim();
                        if (currEntry.startsWith("<td>")) {
                            currEntry = currEntry.substring(4, currEntry.length() - 5);
                            double currRate = parseTableEntry(scanner);
                            rates.put(abbreviations.get(currEntry), currRate);
                        }
                    } else if (currEntry.contains("</table>")) {
                        break;
                    }
                }
            }
        }
        scanner.close();
        rates.put("USD", 1.0);
        // Reshuffle rates if defaultCurrency not USD
        if (!defaultCurrency.equals("USD")) {
            // Get conversion rate from USD to default
            double fromUSD = 1 / rates.get(defaultCurrency);
            for (String currency : rates.keySet()) {
                rates.put(currency, rates.get(currency) * fromUSD);
            }
        }
        return rates;
    }

    /**
     * Parses the given table entry for the current rate to defaultCurrency
     * @requires Scanner is pointing to a currency rate table entry
     * @param scanner Scanner for rates table HTML
     * @return Conversion rate from abbrev to defaultCurrency
     */
    private double parseTableEntry(Scanner scanner) {
        scanner.nextLine();
        String rate = scanner.nextLine().trim();
        rate = rate.substring(rate.indexOf('>') + 1);
        rate = rate.substring(rate.indexOf('>') + 1, rate.length() - 9);
        return Double.parseDouble(rate);
    }

    /**
     * Creates a scanner for the X_Rates rates page
     * @return Scanner for currency rates site
     * @throws IOException If no Internet connection or connection interrupted
     */
    private Scanner getScanner() throws IOException {
        URL site = new URL(X_RATES);
        URLConnection connection = site.openConnection();
        return new Scanner(connection.getInputStream());
    }
}
