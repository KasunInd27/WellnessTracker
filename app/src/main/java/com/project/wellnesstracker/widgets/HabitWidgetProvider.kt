package com.project.wellnesstracker.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
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

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
