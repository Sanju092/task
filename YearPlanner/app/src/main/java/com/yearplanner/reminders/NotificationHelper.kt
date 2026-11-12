package com.yearplanner.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yearplanner.R
import com.yearplanner.ui.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "year_planner_reminders"
    private const val CHANNEL_NAME = "Task Reminders"
    private const val CHANNEL_DESC = "Notifications for task reminders and due dates"

    init {
        // Channel will be created when first needed
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun postReminderNotification(
        context: Context,
        reminderId: String,
        occurrenceId: String,
        title: String = "Task Reminder",
        content: String = "You have a task due"
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.yearplanner.action.VIEW_OCCURRENCE"
            putExtra("occurrence_id", occurrenceId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()

        val manager = NotificationManagerCompat.from(context)
        
        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                manager.notify(reminderId.hashCode(), notification)
            }
        } else {
            manager.notify(reminderId.hashCode(), notification)
        }
    }

    fun cancelNotification(context: Context, reminderId: String) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(reminderId.hashCode())
    }

    fun cancelAllNotifications(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancelAll()
    }
}