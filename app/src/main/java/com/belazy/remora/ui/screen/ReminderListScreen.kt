package com.belazy.remora.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.belazy.remora.R
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.ui.components.ReminderItem
import com.belazy.remora.viewmodel.ReminderViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderListScreen(
    navController: NavController,
    viewModel: ReminderViewModel
) {
    val context = LocalContext.current
    val reminders = viewModel.allReminders.collectAsState(initial = emptyList())

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val remindersForSelectedDate = reminders.value.filter {
        it.dueDateTime.toLocalDate() == selectedDate
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_edit_reminder") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Scrollable horizontal calendar
            CalendarStrip(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Reminder list for selected date
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                if (remindersForSelectedDate.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No reminders for this date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(remindersForSelectedDate) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onClick = {
                                viewModel.setCurrentReminder(reminder)
                                navController.navigate("add_edit_reminder")
                            },
                            onCompleteToggle = {
                                val updated = reminder.copy(isCompleted = !reminder.isCompleted)
                                viewModel.update(updated, context, reschedule = false)
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val days = (0..14).map { today.plusDays(it.toLong()) } // show next 2 weeks

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(days) { date ->
            val isSelected = date == selectedDate
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .clickable { onDateSelected(date) }
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else Color.Transparent,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
