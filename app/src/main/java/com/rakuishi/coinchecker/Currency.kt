package com.rakuishi.coinchecker

import android.content.Context
import android.support.annotation.DrawableRes
import java.util.*

class Currency private constructor(internal var id: Int, internal var name: String, internal var unit: String) {

    val pair: String
        get() = unit + "_jpy"

    @DrawableRes
    fun getIconResId(context: Context): Int {
        return context.resources.getIdentifier("icon_" + unit, "drawable", context.packageName)
    }

    companion object {

        // region pref

        private val PREFS_NAME = "com.rakuishi.coinchecker.currency"
        private val PREF_PREFIX_KEY = "pref_"

        fun saveCurrencyPref(context: Context, appWidgetId: Int, currencyId: Int) {
            val editor = context.getSharedPreferences(PREFS_NAME, 0).edit()
            editor.putInt(PREF_PREFIX_KEY + appWidgetId, currencyId)
            editor.apply()
        }

        fun loadCurrencyPref(context: Context, appWidgetId: Int): Currency? {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val currencyId = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, 0)
            return getCurrency(currencyId)
        }

        fun deleteCurrencyPref(context: Context, appWidgetId: Int) {
            val editor = context.getSharedPreferences(PREFS_NAME, 0).edit()
            editor.remove(PREF_PREFIX_KEY + appWidgetId)
            editor.apply()
        }

        // endregion

        // region defined currencies

        val currencies: ArrayList<Currency>
            get() {
                val coins = ArrayList<Currency>()
                for (id in 0..11) {
                    getCurrency(id)?.let { coins.add(it) }
                }
                return coins
            }

        fun getCurrency(id: Int): Currency? {
            when (id) {
                0 -> return Currency(0, "Bitcoin", "btc")
                1 -> return Currency(1, "Ethereum", "eth")
                2 -> return Currency(2, "Ethereum Classic", "etc")
                3 -> return Currency(3, "LISK", "lsk")
                4 -> return Currency(4, "Factom", "fct")
                5 -> return Currency(5, "Monero", "xmr")
                6 -> return Currency(6, "Augur", "rep")
                7 -> return Currency(7, "Ripple", "xrp")
                8 -> return Currency(8, "Zcash", "zec")
                9 -> return Currency(9, "NEM", "xem")
                10 -> return Currency(10, "Litecoin", "ltc")
                11 -> return Currency(11, "DASH", "dash")
                else -> return null
            }
        }
    }

    // endregion
}
