package com.project.wellnesstracker.receivers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.project.wellnesstracker.MainActivity
import com.project.wellnesstracker.R

/**
 * 🆕 Improved Class:
 * Triggered by AlarmManager and shows a hydration reminder notification.
 * It also reschedules itself for continuous reminders (e.g., every 1 minute).
 */
class HydrationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // ✅ Debug message for emulator: helps confirm the receiver triggered
        Toast.makeText(context, "💧 Hydration reminder triggered!", Toast.LENGTH_SHORT).show()
        android.util.Log.d("HydrationReceiver", "Alarm triggered — showing notification.")

        showNotification(context)

        // 🔁 Reschedule next alarm for 1 minute later (so it repeats)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextIntent = Intent(context, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule exact alarm 1 minute later (even if device idle)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60 * 1000L, // 1 minute later
            pendingIntent
        )
    }

    private fun showNotification(context: Context) {
        val channelId = "hydration_reminders"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8+ (Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to drink water"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open MainActivity when tapped
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "settings")
            putExtra("source", "hydration_alarm")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to Hydrate!")
            .setContentText("Drink some water 💧")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Use unique ID so notifications don’t overwrite
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
