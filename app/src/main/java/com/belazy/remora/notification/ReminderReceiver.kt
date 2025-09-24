package com.belazy.remora.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.belazy.remora.data.model.Priority
import com.belazy.remora.data.model.Reminder
import java.time.LocalDateTime

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        var currentRingtone: Ringtone? = null
        const val ACTION_STOP_RINGTONE = "com.belazy.remora.STOP_RINGTONE"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP_RINGTONE) {
            currentRingtone?.stop()
            currentRingtone = null
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val title = intent.getStringExtra("title") ?: "Reminder"
        val description = intent.getStringExtra("description") ?: ""
        val priority = intent.getStringExtra("priority") ?: "LOW"
        val id = intent.getIntExtra("id", 0)
        val dueDateTimeString = intent.getStringExtra("dueDateTime") // Retrieve dueDateTime

        // Parse dueDateTime or use current time as fallback
        val dueDateTime = dueDateTimeString?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now()

        val reminder = Reminder(
            id = id,
            title = title,
            description = description,
            dueDateTime = dueDateTime,
            priority = Priority.valueOf(priority)
        )

        // Play continuous alarm sound
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        currentRingtone = RingtoneManager.getRingtone(context, alarmUri)
        currentRingtone?.play()

        NotificationUtils.showFullScreenAlarm(context, reminder)
    }
}