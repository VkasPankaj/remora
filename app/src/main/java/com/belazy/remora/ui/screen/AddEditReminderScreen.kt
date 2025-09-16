package com.belazy.remora.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.belazy.remora.data.model.Priority
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.utils.DateTimeUtils
import com.belazy.remora.viewmodel.ReminderViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEditReminderScreen(
    navController: NavController,
    viewModel: ReminderViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var priority by remember { mutableStateOf(Priority.LOW) }

    val currentReminder by viewModel.currentReminder.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(currentReminder) {
        currentReminder?.let {
            title = it.title
            description = it.description ?: ""
            dueDateTime = it.dueDateTime
            priority = it.priority
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ Date & Time picker
        DateTimePickerField(
            dateTime = dueDateTime,
            onDateTimeSelected = { dueDateTime = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Priority dropdown
        PriorityDropdown(priority) { priority = it }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val reminder = Reminder(
                    id = currentReminder?.id ?: 0,
                    title = title,
                    description = description,
                    dueDateTime = dueDateTime,
                    priority = priority,
                    isCompleted = currentReminder?.isCompleted ?: false
                )
                if (currentReminder == null) {
                    viewModel.insert(reminder, context )
                } else {
                    viewModel.update(reminder,context)
                }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Reminder")
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateTimePickerField(
    dateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    val formatted = DateTimeUtils.formatDateTime(dateTime)

    OutlinedTextField(
        value = formatted,
        onValueChange = {},
        label = { Text("Due Date & Time") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // First show DatePicker
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, dateTime.year)
                    set(Calendar.MONTH, dateTime.monthValue - 1)
                    set(Calendar.DAY_OF_MONTH, dateTime.dayOfMonth)
                }

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val pickedDate = LocalDate.of(year, month + 1, day)

                        // Then show TimePicker
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                val pickedTime = LocalTime.of(hour, minute)
                                onDateTimeSelected(LocalDateTime.of(pickedDate, pickedTime))
                            },
                            dateTime.hour,
                            dateTime.minute,
                            true
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
        readOnly = true,
        enabled = false // 👈 prevents keyboard from opening
    )
}


@Composable
fun PriorityDropdown(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedPriority.name,
            onValueChange = {},
            label = { Text("Priority") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true } // triggers dropdown
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth() // ensures it aligns with text field
        ) {
            Priority.entries.forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priority.name) },
                    onClick = {
                        onPrioritySelected(priority)
                        expanded = false
                    }
                )
            }
        }
    }
}
