package com.clipboardreminder.data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.clipboardreminder.domain.repository.NotificationRepository
import com.clipboardreminder.notification.ReminderNotificationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationRepository {

    companion object {
        private const val TAG = "NotificationRepo"
        // WorkManager minimum periodic interval is 15 minutes
        private const val MIN_INTERVAL_MINUTES = 15L
    }

    override suspend fun schedule(reminderId: Long, intervalMinutes: Int) {
        // Enforce minimum interval (WorkManager requires >= 15min)
        val safeInterval = maxOf(intervalMinutes.toLong(), MIN_INTERVAL_MINUTES)

        Log.d(TAG, "Agendando notificação para reminder=$reminderId a cada $safeInterval min")

        val inputData = Data.Builder()
            .putLong(ReminderNotificationWorker.KEY_REMINDER_ID, reminderId)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(
            safeInterval, TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .setConstraints(constraints)
            .setInitialDelay(safeInterval, TimeUnit.MINUTES) // First fire after interval
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "notification_reminder_$reminderId",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )

        Log.d(TAG, "Notificação agendada com sucesso: ID=$reminderId")
    }

    override suspend fun cancel(reminderId: Long) {
        Log.d(TAG, "Cancelando notificação para reminder=$reminderId")
        WorkManager.getInstance(context).cancelUniqueWork("notification_reminder_$reminderId")
    }
}
