package com.belazy.remora

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
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

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
        private const val REQUEST_EXACT_ALARM = 2001
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // ✅ Create Notification Channel
        NotificationUtils.createNotificationChannel(this)

        // ✅ Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
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
            // Step 1: Guide the user
            AlertDialog.Builder(this)
                .setTitle("Allow Exact Alarms")
                .setMessage(
                    "Remora needs Exact Alarm permission so your reminders trigger " +
                            "at the correct time.\n\nPlease allow it in the next screen."
                )
                .setPositiveButton("Continue") { _, _ ->
                    // Step 2: Open system settings
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivityForResult(intent, REQUEST_EXACT_ALARM)
                }
                .setNegativeButton("Not Now") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        "Reminders may be delayed without Exact Alarm permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .show()
        } else {
            Log.d("AlarmPermission", "App can schedule exact alarms ✅")
        }
    }

    // Step 3: Check again when returning from settings
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EXACT_ALARM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == true) {
                Log.d("AlarmPermission", "Exact alarm granted ✅")
                Toast.makeText(this, "Exact Alarms enabled!", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("AlarmPermission", "Exact alarm still NOT granted ❌")
                Toast.makeText(
                    this,
                    "Reminders may not work on time unless you allow Exact Alarms.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
