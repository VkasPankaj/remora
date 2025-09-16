package com.belazy.remora.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.belazy.remora.data.model.Priority
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.ui.theme.HighPriorityColor
import com.belazy.remora.ui.theme.LowPriorityColor
import com.belazy.remora.ui.theme.MediumPriorityColor
import com.belazy.remora.utils.DateTimeUtils

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderItem(
    reminder: Reminder,
    onClick: () -> Unit,
    onCompleteToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                when (reminder.priority) {
                    Priority.LOW -> LowPriorityColor.copy(alpha = 0.1f)
                    Priority.MEDIUM -> MediumPriorityColor.copy(alpha = 0.1f)
                    Priority.HIGH -> HighPriorityColor.copy(alpha = 0.1f)
                }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = reminder.isCompleted,
            onCheckedChange = { onCompleteToggle() }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = DateTimeUtils.formatDateTime(reminder.dueDateTime),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}