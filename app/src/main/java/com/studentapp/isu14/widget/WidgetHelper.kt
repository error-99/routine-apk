package com.studentapp.isu14.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.studentapp.isu14.MainActivity

object WidgetHelper {

    fun isPinningSupported(context: Context): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return appWidgetManager.isRequestPinAppWidgetSupported
    }

    fun requestPinWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(context, RoutineWidgetProvider::class.java)

            val successCallback = Intent(context, MainActivity::class.java)
            val successPendingIntent = PendingIntent.getActivity(
                context,
                0,
                successCallback,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            appWidgetManager.requestPinAppWidget(provider, null, successPendingIntent)
            Toast.makeText(context, "Pinned widget prompt opened!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "To add widget: Long press your home screen -> Widgets -> ISU Routine",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
