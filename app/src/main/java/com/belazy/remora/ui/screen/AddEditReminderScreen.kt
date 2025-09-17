package com.belazy.remora.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.belazy.remora.data.model.Priority
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.ui.theme.HighPriorityColor
import com.belazy.remora.ui.theme.LowPriorityColor
import com.belazy.remora.ui.theme.MediumPriorityColor
import com.belazy.remora.viewmodel.ReminderViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/** ---------- State holder ---------- */
data class ReminderFormState @RequiresApi(Build.VERSION_CODES.O) constructor(
    val title: String = "",
    val description: String = "",
    val dueDateTime: LocalDateTime = LocalDateTime.now(),
    val priority: Priority = Priority.LOW
)

fun Priority.color(): Color = when (this) {
    Priority.LOW -> LowPriorityColor
    Priority.MEDIUM -> MediumPriorityColor
    Priority.HIGH -> HighPriorityColor
}

/** ---------- Main Screen ---------- */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEditReminderScreen(
    navController: NavController,
    viewModel: ReminderViewModel
) {
    var formState by remember { mutableStateOf(ReminderFormState()) }
    val currentReminder by viewModel.currentReminder.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(currentReminder) {
        currentReminder?.let {
            formState = formState.copy(
                title = it.title,
                description = it.description.orEmpty(),
                dueDateTime = it.dueDateTime,
                priority = it.priority
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        FloatingBackgroundElements()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = if (currentReminder == null) "Create New Task" else "Edit Task",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ReminderForm(formState) { updated ->
                        formState = updated
                    }
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        val reminder = Reminder(
                            id = currentReminder?.id ?: 0,
                            title = formState.title,
                            description = formState.description,
                            dueDateTime = formState.dueDateTime,
                            priority = formState.priority,
                            isCompleted = currentReminder?.isCompleted ?: false
                        )
                        if (currentReminder == null) viewModel.insert(reminder, context)
                        else viewModel.update(reminder, context)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = formState.priority.color()
                    ),
                    enabled = formState.title.isNotBlank()
                ) {
                    Text(
                        if (currentReminder == null) "Create Reminder" else "Update Reminder",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/** ---------- Reminder Form ---------- */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderForm(
    formState: ReminderFormState,
    onFormChange: (ReminderFormState) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d")
    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")

    val dateText = formState.dueDateTime.format(dateFormatter)
    val timeText = formState.dueDateTime.format(timeFormatter)

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // 🔔 Reminder Title
        MinimalTextField(
            value = formState.title,
            onValueChange = { onFormChange(formState.copy(title = it)) },
            label = "Reminder",
            placeholder = "e.g., Buy groceries, Call client"
        )

        // 📝 Description
        MinimalTextField(
            value = formState.description,
            onValueChange = { onFormChange(formState.copy(description = it)) },
            label = "Description",
            placeholder = "Add more details about the task",
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        // 📅 Date + ⏰ Time pickers
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, formState.dueDateTime.year)
                        set(Calendar.MONTH, formState.dueDateTime.monthValue - 1)
                        set(Calendar.DAY_OF_MONTH, formState.dueDateTime.dayOfMonth)
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val pickedDate = LocalDate.of(year, month + 1, day)
                            onFormChange(
                                formState.copy(
                                    dueDateTime = LocalDateTime.of(
                                        pickedDate, formState.dueDateTime.toLocalTime()
                                    )
                                )
                            )
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(dateText, fontWeight = FontWeight.Medium)
            }

            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val pickedTime = LocalTime.of(hour, minute)
                            onFormChange(
                                formState.copy(
                                    dueDateTime = LocalDateTime.of(
                                        formState.dueDateTime.toLocalDate(), pickedTime
                                    )
                                )
                            )
                        },
                        formState.dueDateTime.hour,
                        formState.dueDateTime.minute,
                        false
                    ).show()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(timeText, fontWeight = FontWeight.Medium)
            }
        }

        // 🚦 Priority segmented buttons
        Column {
            Text("Priority", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Priority.values().forEach { priority ->
                    val selected = priority == formState.priority
                    OutlinedButton(
                        onClick = { onFormChange(formState.copy(priority = priority)) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) priority.color() else Color.Transparent,
                            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(priority.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
    }
}

/** ---------- Minimal Apple-like TextField ---------- */
@Composable
fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/** ---------- Background Floating Shapes ---------- */
@Composable
fun FloatingBackgroundElements() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 50f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "offset1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -30f,
        animationSpec = infiniteRepeatable(
            tween(6000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "offset2"
    )

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .size(120.dp)
                .offset(x = (-20).dp + offset1.dp, y = 80.dp + offset2.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(LowPriorityColor.copy(alpha = 0.1f))
                .blur(20.dp)
        )
        Box(
            Modifier
                .size(80.dp)
                .offset(x = 280.dp + offset2.dp, y = 200.dp + offset1.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(MediumPriorityColor.copy(alpha = 0.1f))
                .blur(15.dp)
        )
    }
}
