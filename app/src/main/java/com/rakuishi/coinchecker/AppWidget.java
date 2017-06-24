package com.rakuishi.coinchecker;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
    private OkHttpClient client = new OkHttpClient();
    private Moshi moshi = new Moshi.Builder().build();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = AppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // updateAppWidget(context, appWidgetManager, appWidgetId);
            requestRate(appWidgetId, "xrp_jpy");
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

    void requestRate(int appWidgetId, final String pair) {
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
                JsonAdapter<Rate> adapter = moshi.adapter(Rate.class);
                Rate rate = adapter.fromJson(response.body().string());
                Log.d(TAG, pair + ": " + rate.rate);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // do something on Main Thread
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
}

