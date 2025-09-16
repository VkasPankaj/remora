package com.belazy.remora.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.belazy.remora.R
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.ui.components.ReminderItem
import com.belazy.remora.viewmodel.ReminderViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderListScreen(
    navController: NavController,
    viewModel: ReminderViewModel
) {
    val context = LocalContext.current
    val reminders = viewModel.allReminders.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_edit_reminder") }) {
                Icon(painterResource(id = R.drawable.ic_launcher_background), contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(reminders.value) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onClick = {
                        viewModel.setCurrentReminder(reminder)
                        navController.navigate("add_edit_reminder")
                    },
                    onCompleteToggle = {
                        val updated = reminder.copy(isCompleted = !reminder.isCompleted)
                        viewModel.update(updated, context) // 🔹 pass context to schedule notification
                    }
                )
            }
        }
    }
}
