package com.rakuishi.coinchecker;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

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

    public static ArrayList<Currency> getCurrencies() {
        ArrayList<Currency> coins = new ArrayList<>();
        for (int id = 1; id <= 12; id++) {
            coins.add(getCurrency(id));
        }
        return coins;
    }

    @NonNull
    public static Currency getCurrency(int id) {
        switch (id) {
            case 0:
            default:
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
        }
    }
}
