package com.belazy.remora.notification

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.belazy.remora.data.model.Priority
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.ui.theme.Pink80
import com.belazy.remora.ui.theme.Purple80
import com.belazy.remora.ui.theme.RemoraTheme
import com.belazy.remora.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AlarmActivity : ComponentActivity() {

    private val viewModel: ReminderViewModel by viewModels()

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

        // Extract reminder details from intent
        val reminderId = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "Reminder"
        val description = intent.getStringExtra("description") ?: ""
        val priority = intent.getStringExtra("priority") ?: "LOW"
        val dueDateTimeString = intent.getStringExtra("dueDateTime")
        val dueDateTime = dueDateTimeString?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now()
        setContent {
            RemoraTheme {
                AlarmScreen(
                    reminderId = reminderId,
                    title = title,
                    description = description,
                    priority = priority,
                    dueDateTime = dueDateTime,
                    onDismiss = { reminder ->
                        // Stop the ringtone started by ReminderReceiver
                        val stopIntent = Intent(ReminderReceiver.ACTION_STOP_RINGTONE).apply {
                            setPackage(packageName) // Ensure broadcast is sent to this app
                        }
                        sendBroadcast(stopIntent)

                        // Mark reminder as completed
                        if (reminder != null && reminder.id != 0) {
                            viewModel.update(
                                reminder.copy(isCompleted = true),
                                this@AlarmActivity,
                                reschedule = false
                            )
                        }

                        // 🔴 Close the entire app
                        finishAffinity() // Finish this and all parent activities
                        finishAndRemoveTask() // Remove app from Recents
                        android.os.Process.killProcess(android.os.Process.myPid()) // Kill process
                    }

                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure ringtone is stopped if activity is destroyed without dismissal
        val stopIntent = Intent(ReminderReceiver.ACTION_STOP_RINGTONE).apply {
            setPackage(packageName)
        }
        sendBroadcast(stopIntent)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmScreen(
    reminderId: Int,
    title: String,
    description: String,
    priority: String,
    dueDateTime: LocalDateTime,
    onDismiss: (Reminder?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val buttonScale = remember { Animatable(1f) }

    // Gradient background (modern look)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Purple80,Pink80
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Reminder Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Optional Description
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                // Due time
                Text(
                    text = "⏰ ${dueDateTime.toLocalTime()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Priority badge
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = priority,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dismiss button
                Button(
                    onClick = {
                        scope.launch {
                            buttonScale.animateTo(
                                targetValue = 0.95f,
                                animationSpec = tween(durationMillis = 100)
                            )
                            buttonScale.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 100)
                            )
                            val reminder = Reminder(
                                id = reminderId,
                                title = title,
                                description = description,
                                dueDateTime = dueDateTime,
                                priority = Priority.valueOf(priority)
                            )
                            onDismiss(reminder)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(buttonScale.value),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Dismiss & Complete",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
