package com.belazy.remora.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderListScreen(
    navController: NavController,
    viewModel: ReminderViewModel
) {
    val context = LocalContext.current
    val reminders by viewModel.allReminders.collectAsState(initial = emptyList())
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val remindersForSelectedDate = remember(reminders, selectedDate) {
        reminders.filter { it.dueDateTime.toLocalDate() == selectedDate }
            .sortedBy { it.dueDateTime }
    }

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remora") },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Open Calendar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {  viewModel.setCurrentReminder(null)
                    navController.navigate("add_edit_reminder") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
            // Horizontal scrollable calendar strip
            CalendarStrip(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Reminder list for the selected date
            if (remindersForSelectedDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reminders for this date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                    }
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(millis),
                            ZoneId.systemDefault()
                        ).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp),
            )
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
    val startDate = today.minusYears(1)
    val endDate = today.plusYears(1)
    val days = remember {
        generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .toList()
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        val index = days.indexOf(selectedDate)
        if (index >= 0) {
            lazyListState.animateScrollToItem(index, scrollOffset = -200)
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { date ->
            val isSelected = date == selectedDate
            val isToday = date == today
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .clickable { onDateSelected(date) }
                    .background(
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            else -> Color.Transparent
                        },
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else if (isToday) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else if (isToday) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderItem(
    reminder: Reminder,
    onClick: () -> Unit,
    onCompleteToggle: () -> Unit
) {
    val isCompleted = reminder.isCompleted
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large,

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: Priority badge and Time badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Priority Badge
                PriorityBadge(
                    priority = reminder.priority.toString(),
                    isCompleted = isCompleted
                )

                // Time badge
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = reminder.dueDateTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Content and Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Title and Description
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                            fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold
                        ),
                        color = if (isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Description (if available)
                    if (!reminder.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reminder.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCompleted)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right side: Checkbox
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onCompleteToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                        checkmarkColor = Color.White,
                        disabledUncheckedColor = Color.Transparent,
                        disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
fun PriorityBadge(
    priority: String,
    isCompleted: Boolean
) {
    val (color, textColor) = when (priority.lowercase()) {
        "high" -> Color(0xFFE53E3E) to Color.White  // Red
        "medium" -> Color(0xFFECC94B) to Color.Black  // Yellow
        "low" -> Color(0xFF38A169) to Color.White  // Green
        else -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .background(
                color = if (isCompleted) color.copy(alpha = 0.3f) else color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (isCompleted) textColor.copy(alpha = 0.7f) else textColor
        )
    }
}