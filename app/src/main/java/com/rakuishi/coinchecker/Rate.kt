package com.rakuishi.coinchecker

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

class Rate(val currencyId: Int, val value: String, val updatedAtMillis: Long) {

    companion object {

        // region pref

        private val PREFS_NAME = "com.rakuishi.coinchecker.rate"
        private val PREF_PREFIX_KEY = "pref_"

        fun saveRatePref(context: Context, rate: Rate) {
            val editor = context.getSharedPreferences(PREFS_NAME, 0).edit()
            editor.putString(PREF_PREFIX_KEY + rate.currencyId, getJsonAdapter().toJson(rate))
            editor.apply()
        }

        fun loadRatePref(context: Context, currencyId: Int): Rate? {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            return getJsonAdapter().fromJson(prefs.getString(PREF_PREFIX_KEY + currencyId, ""))
        }

        fun deleteRatePref(context: Context, currencyId: Int) {
            val editor = context.getSharedPreferences(PREFS_NAME, 0).edit()
            editor.remove(PREF_PREFIX_KEY + currencyId)
            editor.apply()
        }

        fun getJsonAdapter(): JsonAdapter<Rate> {
            val moshi = Moshi.Builder().build()
            return moshi.adapter<Rate>(Rate::class.java)
        }

        // endregion
    }
}