package com.belazy.remora.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.belazy.remora.R
import com.belazy.remora.data.model.Reminder
import java.time.ZoneId

object NotificationUtils {

    const val CHANNEL_ID = "reminder_channel"
    private const val CHANNEL_NAME = "Reminders"

    /**
     * Create notification channel for Android O+
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * Show immediate notification
     */
    fun showNotification(context: Context, title: String, description: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Schedule exact notification with AlarmManager
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleNotification(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("description", reminder.description ?: "")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = reminder.dueDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ must check if exact alarms are allowed
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fallback: approximate alarm
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                // Pre-Android 12
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Optional: fallback using WorkManager or notify user
        }
    }

    /**
     * Cancel previously scheduled alarm
     */
    fun cancelNotification(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun scheduleAlarm(reminder: Reminder, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", reminder.id)
            putExtra("title", reminder.title)
            putExtra("description", reminder.description)
            putExtra("showAlarmScreen", true) // flag to show alarm activity
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = reminder.dueDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showFullScreenAlarm(context: Context, reminder: Reminder) {
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("description", reminder.description ?: "")
            putExtra("priority", reminder.priority.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminder.id,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create a notification channel for full-screen notifications
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null) // optional: system default alarm sound
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(reminder.title)
            .setContentText(reminder.description ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()

        manager.notify(reminder.id, notification)
    }


}
