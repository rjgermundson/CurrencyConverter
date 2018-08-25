package com.example.riley.currencyconverter.CurrencyScraper;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

/**
 * The WebScraper will take the currency rates from the Internet and create or update
 * a local database for currency abbreviations and currency rates
 *
 * Returns a null table of currencies if connection was interrupted
 */
public class WebScraper extends AsyncTask<String, Void, Hashtable<String, Double>> {

    private static final String CURRENCY_SITE = "https://www.mycurrency.net";
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

        List<String> currencyCodes = new ArrayList<>();
        List<Double> currencyRates = new ArrayList<>();
        while (scanner.hasNext()) {
            String currLine = scanner.nextLine();
            if (currLine.contains("<table class='table table-bordered table-striped'")){
                while (scanner.hasNextLine()) {
                    currLine = scanner.nextLine();
                    if (currLine.contains("<tr class='country'")) {
                        scanner.nextLine();
                        scanner.nextLine();
                        String currencyCode = scanner.nextLine().trim().substring(20, 23);
                        while (!currLine.contains("class='money' data-rate=")) {
                            currLine = scanner.nextLine();
                        }
                        String currRate = currLine.trim().substring(25);
                        currRate = currRate.substring(0, currRate.indexOf('\''));
                        currencyCodes.add(currencyCode);
                        currencyRates.add(Double.parseDouble(currRate));
                    }
                }
            }
        }

        scanner.close();
        Hashtable<String, Double> rates = new Hashtable<>();
        for (int i = 0; i < currencyCodes.size(); i++) {
            rates.put(currencyCodes.get(i), currencyRates.get(i));
        }
        rates.put("USD", 1.0);
        // Reshuffle rates if defaultCurrency not USD
        if (!defaultCurrency.equals("USD")) {
            // Get conversion rate from USD to default
            double fromUSD = 1 / rates.get(defaultCurrency);
            for (String currency : rates.keySet()) {
                rates.put(currency, rates.get(currency) * fromUSD);
            }
        }
        System.err.println(rates.size());
        for (String code : rates.keySet()) {
            System.err.println(code + " : " + rates.get(code));
        }
        return rates;
    }

    /**
     * Creates a scanner for the X_Rates rates page
     * @return Scanner for currency rates site
     * @throws IOException If no Internet connection or connection interrupted
     */
    private Scanner getScanner() throws IOException {
        URL site = new URL(CURRENCY_SITE);
        URLConnection connection = site.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        return new Scanner(connection.getInputStream());
    }
}
