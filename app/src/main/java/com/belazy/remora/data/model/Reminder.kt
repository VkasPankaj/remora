package com.belazy.remora.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val dueDateTime: LocalDateTime,
    val priority: Priority = Priority.LOW,
    var isCompleted: Boolean = false
)

enum class Priority {
    LOW, MEDIUM, HIGH
}