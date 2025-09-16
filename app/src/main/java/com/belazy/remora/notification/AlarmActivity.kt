package com.belazy.remora.notification

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.belazy.remora.ui.theme.RemoraTheme

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake up the device
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Get default alarm sound
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ringtone = RingtoneManager.getRingtone(this, alarmUri)

        // Play ringtone safely
        ringtone?.play()

        setContent {
            RemoraTheme {
                AlarmScreen(
                    title = intent.getStringExtra("title") ?: "Reminder",
                    description = intent.getStringExtra("description") ?: "",
                    onDismiss = {
                        ringtone?.stop()
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun AlarmScreen(title: String, description: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}
