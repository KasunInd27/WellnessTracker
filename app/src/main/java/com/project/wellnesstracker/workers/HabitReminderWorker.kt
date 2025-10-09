// workers/HabitReminderWorker.kt

package com.project.wellnesstracker.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.project.wellnesstracker.MainActivity
import com.project.wellnesstracker.R
import com.project.wellnesstracker.utils.DataManager
import kotlinx.coroutines.delay

class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dataManager = DataManager(applicationContext)

        // Only show notification if there are incomplete non-water habits
        if (dataManager.hasIncompleteNonWaterHabits()) {
            showNotification()
        }

        delay(1000) // 1 second minimum between notifications

        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val channelId = "habit_reminders"

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for your daily habits"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create explicit intent to open MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "habits")
            putExtra("source", "notification")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dataManager = DataManager(applicationContext)
        val incompleteHabits = dataManager.getNonWaterHabits().filter { !it.isCompletedToday() }
        val habitCount = incompleteHabits.size

        val title = "Time for Your Habits! ⏰"
        val text = when {
            habitCount == 1 -> "You have 1 habit waiting to be completed"
            habitCount > 1 -> "You have $habitCount habits waiting to be completed"
            else -> "Keep up with your daily habits!"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(1002, notification)
    }
}