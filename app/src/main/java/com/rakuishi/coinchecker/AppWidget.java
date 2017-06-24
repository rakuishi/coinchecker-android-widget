package com.rakuishi.coinchecker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

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
    private OkHttpClient client = new OkHttpClient();
    private Moshi moshi = new Moshi.Builder().build();

    // region AppWidgetProvider

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            requestRate(context, appWidgetManager, appWidgetId, "xrp_jpy");
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            AppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
            cancel(appWidgetId);
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

    void requestRate(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId,
                     final String pair) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final Request request = new Request.Builder()
                .url("https://coincheck.com/api/rate/" + pair)
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
                        updateAppWidget(context, appWidgetManager, appWidgetId, pair, rateResponse);
                    }
                });
            }
        });
    }

    void cancel(int appWidgetId) {
        for (Call call : client.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(getRequestTag(appWidgetId))) {
                call.cancel();
                break;
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (call.request().tag().equals(getRequestTag(appWidgetId))) {
                call.cancel();
                break;
            }
        }
    }

    String getRequestTag(int appWidgetId) {
        return String.valueOf(appWidgetId);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                String pair, @Nullable RateResponse rateResponse) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setOnClickPendingIntent(R.id.appwidget_root, onClickRootView(context));
        views.setTextViewText(R.id.appwidget_coin_name_text, pair);
        if (rateResponse != null) {
            views.setTextViewText(R.id.appwidget_coin_price_text, rateResponse.rate);
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

