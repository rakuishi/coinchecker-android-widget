package com.rakuishi.coinchecker;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * The configuration screen for the {@link AppWidget AppWidget} AppWidget.
 */
public class AppWidgetConfigureActivity extends Activity {

    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public AppWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.app_widget_configure);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            );
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        CurrencyAdapter adapter = new CurrencyAdapter(this);
        adapter.setCallback(new CurrencyAdapter.Callback() {
            @Override
            public void onClickCurrency(int currencyId) {
                Context context = AppWidgetConfigureActivity.this;
                Currency.saveCurrencyPref(context, appWidgetId, currencyId);

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                AppWidget.updateAppWidget(context, appWidgetManager, appWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}

