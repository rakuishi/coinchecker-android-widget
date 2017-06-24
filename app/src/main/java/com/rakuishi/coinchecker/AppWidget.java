package com.rakuishi.coinchecker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
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

            // Reload rate
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, AppWidget.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    // endregion

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final Currency currency = Currency.loadCurrencyPref(context, appWidgetId);
        if (currency == null) {
            // Currency is not saved in pref.
            return;
        }
        final OkHttpClient client = new OkHttpClient();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final Request request = new Request.Builder()
                .url("https://coincheck.com/api/rate/" + currency.getPair())
                .get()
                .tag(String.valueOf(appWidgetId))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateAppWidgetRemoteViews(context, appWidgetManager, appWidgetId, null);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String parsedRate;
                try {
                    // noinspection ConstantConditions
                    JSONObject json = new JSONObject(response.body().string());
                    parsedRate = json.getString("rate");
                } catch (JSONException e) {
                    parsedRate = null;
                }

                final String rate = parsedRate;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateAppWidgetRemoteViews(context, appWidgetManager, appWidgetId, rate);
                    }
                });
            }
        });
    }

    private static void updateAppWidgetRemoteViews(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                                   @Nullable String rate) {
        final Currency currency = Currency.loadCurrencyPref(context, appWidgetId);
        if (currency == null) {
            // This if statement is unnecessary.
            // The currency is checked in `updateAppWidget()` before this method.
            return;
        }
        final String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setOnClickPendingIntent(R.id.appwidget_root, onClickRootView(context));
        views.setImageViewIcon(R.id.appwidget_image, Icon.createWithResource(context, currency.getIconResId(context)));
        views.setTextViewText(R.id.appwidget_unit_text, currency.unit.toUpperCase());
        views.setTextViewText(R.id.appwidget_name_text, currency.name);
        views.setTextViewText(R.id.appwidget_time_text, time);
        if (TextUtils.isEmpty(rate)) {
            views.setTextViewText(R.id.appwidget_rate_text, context.getString(R.string.failed));
        } else {
            String formattedRate;
            try {
                formattedRate = String.format("%.2f", Double.valueOf(rate));
            } catch (NullPointerException e) {
                formattedRate = rate;
            }
            views.setTextViewText(R.id.appwidget_rate_text, "¥" + formattedRate);
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

