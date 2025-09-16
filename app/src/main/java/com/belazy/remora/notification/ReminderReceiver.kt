package com.belazy.remora.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.belazy.remora.data.model.Priority
import com.belazy.remora.data.model.Reminder

class ReminderReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val description = intent.getStringExtra("description") ?: ""
        val priority = intent.getStringExtra("priority") ?: "LOW"
        val id = intent.getIntExtra("id", 0)

        val reminder = Reminder(
            id = id,
            title = title,
            description = description,
            dueDateTime = java.time.LocalDateTime.now(),
            priority = Priority.valueOf(priority)
        )

        NotificationUtils.showFullScreenAlarm(context, reminder)

        // Vibrate continuously
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 1000, 1000) // wait, vibrate, sleep
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))

        // Play default alarm ringtone
        val ringtone = RingtoneManager.getRingtone(
            context,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        ringtone?.play()
    }
}
