package com.rakuishi.coinchecker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link AppWidgetConfigureActivity AppWidgetConfigureActivity}
 */
public class AppWidget extends AppWidgetProvider {

    private static final String TAG = AppWidget.class.getSimpleName();
    private static final String ROOT_VIEW_CLICK_ACTION = "app_widget_root_view_click_action";
    private static final String PACKAGE_NAME_COINCHECK = "jp.coincheck.android";

    // region AppWidgetProvider

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            Currency.deleteCurrencyPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ROOT_VIEW_CLICK_ACTION)) {
            openAppIfPossible(context, PACKAGE_NAME_COINCHECK);
        }
    }

    // endregion

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final Currency currency = Currency.loadCurrencyPref(context, appWidgetId);
        final OkHttpClient client = new OkHttpClient();
        final Moshi moshi = new Moshi.Builder().build();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final Request request = new Request.Builder()
                .url("https://coincheck.com/api/rate/" + currency.getPair())
                .get()
                .tag(String.valueOf(appWidgetId))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // do something
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JsonAdapter<RateResponse> adapter = moshi.adapter(RateResponse.class);
                final RateResponse rateResponse = adapter.fromJson(response.body().string());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // do something on Main Thread
                        updateAppWidgetRemoteViews(context, appWidgetManager, appWidgetId, rateResponse);
                    }
                });
            }
        });
    }

    static void updateAppWidgetRemoteViews(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                           @Nullable RateResponse rateResponse) {
        final Currency currency = Currency.loadCurrencyPref(context, appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setOnClickPendingIntent(R.id.appwidget_root, onClickRootView(context));
        views.setImageViewIcon(R.id.appwidget_coin_image, Icon.createWithResource(context, currency.getIconResId(context)));
        views.setTextViewText(R.id.appwidget_coin_short_name_text, currency.unit.toUpperCase());
        views.setTextViewText(R.id.appwidget_coin_name_text, currency.name);
        String time = new SimpleDateFormat("kk:mm").format(new Date());
        views.setTextViewText(R.id.appwidget_coin_time_text, time);
        if (rateResponse != null) {
            String rate;
            try {
                rate = String.format("%.3f", Double.valueOf(rateResponse.rate));
            } catch (Exception e) {
                rate = rateResponse.rate;
            }
            views.setTextViewText(R.id.appwidget_coin_price_text, rate + " JPY");
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static PendingIntent onClickRootView(Context context) {
        Intent intent = new Intent(ROOT_VIEW_CLICK_ACTION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static boolean openAppIfPossible(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

