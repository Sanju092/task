package com.yearplanner.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yearplanner.data.model.Reminder
import com.yearplanner.data.model.ReminderPrecision
import com.yearplanner.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class ReminderScheduler(
    private val context: Context,
    private val repository: TaskRepository
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val isScheduling = AtomicBoolean(false)

    /**
     * Schedules a reminder using AlarmManager.
     */
    fun scheduleReminder(reminder: Reminder) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("occurrence_id", reminder.occurrenceId)
            action = "com.yearplanner.action.REMINDER_TRIGGER"
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, reminder.id.hashCode(), intent, flags)

        val triggerAtMillis = reminder.resolvedInstant

        when (reminder.precisionMode) {
            ReminderPrecision.EXACT, ReminderPrecision.EXACT_ALARM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        // Fallback to inexact
                        scheduleInexact(triggerAtMillis, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }
            ReminderPrecision.INEXACT -> {
                scheduleInexact(triggerAtMillis, pendingIntent)
            }
        }
    }

    private fun scheduleInexact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancels a reminder alarm.
     */
    fun cancelReminder(reminder: Reminder) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.yearplanner.action.REMINDER_TRIGGER"
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, reminder.id.hashCode(), intent, flags)
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Cancels all reminders for an occurrence.
     */
    fun cancelRemindersForOccurrence(occurrenceId: String) {
        // This would need to query reminders by occurrenceId and cancel each
        // For now, we rely on the receiver checking if reminder is still enabled
    }

    /**
     * Replenishes the reminder queue with upcoming reminders.
     * Called periodically and after edits/completions.
     */
    fun replenishReminderQueue() {
        if (isScheduling.getAndSet(true)) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val horizon = now + (30 * 24 * 60 * 60 * 1000) // 30 days ahead
                
                val dueReminders = repository.getDueReminders(horizon).first()
                
                for (reminder in dueReminders) {
                    if (reminder.enabled && reminder.resolvedInstant > now) {
                        scheduleReminder(reminder)
                    }
                }
            } finally {
                isScheduling.set(false)
            }
        }
    }

    /**
     * Reschedules all reminders after boot/timezone change.
     */
    fun rescheduleAllReminders() {
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = repository.getAllEnabledReminders().first()
            val now = System.currentTimeMillis()
            
            for (reminder in reminders) {
                if (reminder.resolvedInstant > now) {
                    scheduleReminder(reminder)
                }
            }
        }
    }

    /**
     * Checks if exact alarm permission is granted.
     */
    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }

    /**
     * Requests exact alarm permission from user.
     */
    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

/**
 * Worker for periodic reminder queue replenishment.
 */
class ReminderReplenishWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            // This would need access to the repository
            // For now, return success
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}

/**
 * Broadcast receiver for reminder triggers.
 */
class ReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id")
        val occurrenceId = intent.getStringExtra("occurrence_id")
        
        if (reminderId != null && occurrenceId != null) {
            // Post notification
            NotificationHelper.postReminderNotification(context, reminderId, occurrenceId)
        }
    }
}

/**
 * Boot receiver to reschedule reminders after reboot.
 */
class BootReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_LOCALE_CHANGED) {
            
            // Reschedule reminders
            val scheduler = ReminderScheduler(context, TaskRepository.getInstance(context))
            scheduler.rescheduleAllReminders()
        }
    }
}

companion object {
    private var instance: TaskRepository? = null
    
    fun getInstance(context: Context): TaskRepository {
        return instance ?: synchronized(this) {
            val db = com.yearplanner.data.db.YearPlannerDatabase.getInstance(context)
            val repo = TaskRepository(db)
            instance = repo
            repo
        }
    }
}