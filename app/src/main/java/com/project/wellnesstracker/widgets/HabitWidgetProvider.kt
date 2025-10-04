package com.project.wellnesstracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.project.wellnesstracker.MainActivity
import com.project.wellnesstracker.R
import com.project.wellnesstracker.utils.DataManager

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val dataManager = DataManager(context)
        val percentage = dataManager.getTodayCompletionPercentage()

        val views = RemoteViews(context.packageName, R.layout.widget_habit_progress)
        views.setTextViewText(R.id.widget_percentage, "$percentage%")

        // Create explicit intent to open MainActivity when widget is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("source", "widget")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Update widget when habits are updated
        if (intent.action == "com.project.wellnesstracker.UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, HabitWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, widgetIds)
        }
    }
}