package com.belazy.remora.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.belazy.remora.data.ReminderDatabase
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.notification.NotificationUtils
import com.belazy.remora.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository

    val allReminders: StateFlow<List<Reminder>>
    private val _currentReminder = MutableStateFlow<Reminder?>(null)
    val currentReminder: StateFlow<Reminder?> = _currentReminder

    init {
        val reminderDao = ReminderDatabase.getDatabase(application).reminderDao()
        repository = ReminderRepository(reminderDao)

        allReminders = repository.allReminders
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun setCurrentReminder(reminder: Reminder?) {
        _currentReminder.value = reminder
    }
    @SuppressLint("ScheduleExactAlarm")
    fun insert(reminder: Reminder, context: Context) = viewModelScope.launch {
        repository.insert(reminder)

        // Schedule normal notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.scheduleNotification(context, reminder)
        }

        // Schedule full-screen alarm (opens AlarmActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NotificationUtils.scheduleAlarm(reminder, context)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun update(reminder: Reminder, context: Context) = viewModelScope.launch {
        // Cancel previous alarm
        NotificationUtils.cancelNotification(context, reminder)

        repository.update(reminder)

        // Reschedule both
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.scheduleNotification(context, reminder)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NotificationUtils.scheduleAlarm(reminder, context)
        }
    }

    fun delete(reminder: Reminder, context: Context) = viewModelScope.launch {
        repository.delete(reminder)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.cancelNotification(context, reminder)
        }
    }

}
