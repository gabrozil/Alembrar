package com.clipboardreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.clipboardreminder.domain.repository.AppPreferencesRepository
import com.clipboardreminder.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Handle persistent notification
                val prefs = appPreferencesRepository.getPreferences().first()
                if (prefs.persistentNotificationEnabled) {
                    val serviceIntent = Intent(context, PersistentNotificationService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }

                // Handle reminders
                val reminders = reminderRepository.getRemindersWithNotification()
                val workManager = WorkManager.getInstance(context)
                reminders.forEach { reminder ->
                    val intervalMinutes = reminder.notificationIntervalMinutes ?: return@forEach
                    val inputData = Data.Builder()
                        .putLong(ReminderNotificationWorker.KEY_REMINDER_ID, reminder.id)
                        .build()
                    val request = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(
                        intervalMinutes.toLong(), TimeUnit.MINUTES
                    ).setInputData(inputData).build()
                    workManager.enqueueUniquePeriodicWork(
                        "notification_reminder_${reminder.id}",
                        androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                        request
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
