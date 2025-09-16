package com.belazy.remora

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.belazy.remora.notification.NotificationUtils
import com.belazy.remora.ui.navigation.AppNavHost
import com.belazy.remora.ui.theme.RemoraTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // ✅ Create Notification Channel
        NotificationUtils.createNotificationChannel(this)

        // ✅ Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // ✅ Check exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkExactAlarmPermission()
        }

        setContent {
            RemoraTheme {
                AppNavHost()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkExactAlarmPermission() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (alarmManager?.canScheduleExactAlarms() == false) {
            Log.e("AlarmPermission", "App cannot schedule exact alarms. Request user to allow it!")
            // Open system settings for exact alarm permission
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        } else {
            Log.d("AlarmPermission", "App can schedule exact alarms ✅")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    RemoraTheme {
        AppNavHost()
    }
}
