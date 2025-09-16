package com.belazy.remora.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return dateTime.format(formatter)
    }
}