package com.rakuishi.coinchecker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class Currency {
    int id;
    String name;
    String unit;

    private Currency(int id, String name, String unit) {
        this.id = id;
        this.name = name;
        this.unit = unit;
    }

    public String getPair() {
        return unit + "_jpy";
    }

    @DrawableRes
    public int getIconResId(Context context) {
        return context.getResources().getIdentifier("icon_" + unit, "drawable", context.getPackageName());
    }

    // region pref

    private static final String PREFS_NAME = "com.rakuishi.coinchecker.currency";
    private static final String PREF_PREFIX_KEY = "pref_";

    public static void saveCurrencyPref(Context context, int appWidgetId, int currencyId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId, currencyId);
        prefs.apply();
    }

    public static Currency loadCurrencyPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int currencyId = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, 0);
        return getCurrency(currencyId);
    }

    public static void deleteCurrencyPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    // endregion

    // region defined currencies

    public static ArrayList<Currency> getCurrencies() {
        ArrayList<Currency> coins = new ArrayList<>();
        for (int id = 0; id < 12; id++) {
            coins.add(getCurrency(id));
        }
        return coins;
    }

    @Nullable
    public static Currency getCurrency(int id) {
        switch (id) {
            case 0:
                return new Currency(0, "Bitcoin", "btc");
            case 1:
                return new Currency(1, "Ethereum", "eth");
            case 2:
                return new Currency(2, "Ethereum Classic", "etc");
            case 3:
                return new Currency(3, "LISK", "lsk");
            case 4:
                return new Currency(4, "Factom", "fct");
            case 5:
                return new Currency(5, "Monero", "xmr");
            case 6:
                return new Currency(6, "Augur", "rep");
            case 7:
                return new Currency(7, "Ripple", "xrp");
            case 8:
                return new Currency(8, "Zcash", "zec");
            case 9:
                return new Currency(9, "NEM", "xem");
            case 10:
                return new Currency(10, "Litecoin", "ltc");
            case 11:
                return new Currency(11, "DASH", "dash");
            default:
                return null;
        }
    }

    // endregion
}
