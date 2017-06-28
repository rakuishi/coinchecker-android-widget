package com.rakuishi.coinchecker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.RemoteViews
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.DateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [AppWidgetConfigureActivity]
 */
class AppWidget : AppWidgetProvider() {

    // region AppWidgetProvider

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            Currency.deleteCurrencyPref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ROOT_VIEW_CLICK_ACTION) {
            openAppIfPossible(context, PACKAGE_NAME_COINCHECK)

            // Reload rate
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, AppWidget::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {

        private val ROOT_VIEW_CLICK_ACTION = "app_widget_root_view_click_action"
        private val PACKAGE_NAME_COINCHECK = "jp.coincheck.android"

        // endregion

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val currency = Currency.loadCurrencyPref(context, appWidgetId) ?: // Currency is not saved in pref.
                    return
            val client = OkHttpClient()
            val mainHandler = Handler(Looper.getMainLooper())
            val request = Request.Builder()
                    .url("https://coincheck.com/api/rate/" + currency.pair)
                    .get()
                    .tag(appWidgetId.toString())
                    .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    mainHandler.post { updateAppWidgetRemoteViews(context, appWidgetManager, appWidgetId, null) }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    var parsedRate: String?
                    try {

                        val json = JSONObject(response.body()!!.string())
                        parsedRate = json.getString("rate")
                    } catch (e: JSONException) {
                        parsedRate = null
                    }

                    val rate = parsedRate
                    mainHandler.post { updateAppWidgetRemoteViews(context, appWidgetManager, appWidgetId, rate) }
                }
            })
        }

        private fun updateAppWidgetRemoteViews(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int,
                                               rate: String?) {
            val currency = Currency.loadCurrencyPref(context, appWidgetId) ?: // This if statement is unnecessary.
                    // The currency is checked in `updateAppWidget()` before this method.
                    return
            val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())

            val views = RemoteViews(context.packageName, R.layout.app_widget)
            views.setOnClickPendingIntent(R.id.appwidget_root, onClickRootView(context))
            views.setImageViewIcon(R.id.appwidget_image, Icon.createWithResource(context, currency.getIconResId(context)))
            views.setTextViewText(R.id.appwidget_unit_text, currency.unit.toUpperCase())
            views.setTextViewText(R.id.appwidget_name_text, currency.name)
            views.setTextViewText(R.id.appwidget_time_text, time)
            if (TextUtils.isEmpty(rate)) {
                views.setTextViewText(R.id.appwidget_rate_text, context.getString(R.string.failed))
            } else {
                var formattedRate: String
                try {
                    formattedRate = String.format("%.2f", java.lang.Double.valueOf(rate))
                } catch (e: NullPointerException) {
                    formattedRate = rate!! // 空文字確認済
                }

                views.setTextViewText(R.id.appwidget_rate_text, "¥" + formattedRate)
            }

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun onClickRootView(context: Context): PendingIntent {
            val intent = Intent(ROOT_VIEW_CLICK_ACTION)
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun openAppIfPossible(context: Context, packageName: String): Boolean {
            val manager = context.packageManager
            try {
                val i = manager.getLaunchIntentForPackage(packageName) ?: return false
                i.addCategory(Intent.CATEGORY_LAUNCHER)
                context.startActivity(i)
                return true
            } catch (e: Exception) {
                return false
            }

        }
    }
}

