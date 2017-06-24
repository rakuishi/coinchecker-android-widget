package com.rakuishi.coinchecker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CurrencyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Callback {
        void onClickCurrency(int currencyId);
    }

    private Context context;
    private Callback callback;
    private ArrayList<Currency> currencies;

    public CurrencyAdapter(Context context) {
        this.context = context;
        currencies = Currency.getCurrencies();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_currency, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Currency currency = currencies.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.imageView.setImageResource(currency.getIconResId(context));
        viewHolder.textView.setText(String.format("%s (%s)", currency.name, currency.unit.toUpperCase()));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onClickCurrency(currency.id);
                }
            }
        });
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.currency_image);
            textView = (TextView) itemView.findViewById(R.id.currency_text);
        }
    }
}
