package com.belazy.remora.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.belazy.remora.data.dao.ReminderDao
import com.belazy.remora.data.model.Reminder
import com.belazy.remora.utils.DateTimeConverter

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeConverter::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}