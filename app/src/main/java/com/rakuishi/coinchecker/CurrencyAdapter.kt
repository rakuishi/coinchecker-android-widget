package com.rakuishi.coinchecker

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.util.*

class CurrencyAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Callback {
        fun onClickCurrency(currencyId: Int)
    }

    private var callback: Callback? = null
    private val currencies: ArrayList<Currency> = Currency.currencies

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    override fun getItemCount(): Int {
        return currencies.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_currency, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currency = currencies[position]
        val viewHolder = holder as ViewHolder
        viewHolder.imageView.setImageResource(currency.getIconResId(context))
        viewHolder.textView.text = String.format("%s (%s)", currency.name, currency.unit.toUpperCase())
        viewHolder.itemView.setOnClickListener {
            if (callback != null) {
                callback!!.onClickCurrency(currency.id)
            }
        }
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var imageView: ImageView = itemView.findViewById(R.id.currency_image) as ImageView
        internal var textView: TextView = itemView.findViewById(R.id.currency_text) as TextView
    }
}
