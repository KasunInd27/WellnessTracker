package com.project.wellnesstracker.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.project.wellnesstracker.widgets.HabitWidgetProvider

object WidgetUpdateHelper {

    fun updateWidget(context: Context) {
        val intent = Intent(context, HabitWidgetProvider::class.java).apply {
            action = "com.project.wellnesstracker.UPDATE_WIDGET"
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, HabitWidgetProvider::class.java)
        )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        context.sendBroadcast(intent)
    }
}