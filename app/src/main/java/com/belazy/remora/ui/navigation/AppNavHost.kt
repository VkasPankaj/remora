package com.belazy.remora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.belazy.remora.ui.screen.AddEditReminderScreen
import com.belazy.remora.ui.screen.ReminderListScreen
import com.belazy.remora.viewmodel.ReminderViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val viewModel: ReminderViewModel = viewModel()

    NavHost(navController = navController, startDestination = "reminder_list") {
        composable("reminder_list") {
            ReminderListScreen(navController = navController, viewModel = viewModel)
        }
        composable("add_edit_reminder") {
            AddEditReminderScreen(navController = navController, viewModel = viewModel)
        }
    }
}