package com.clipboardreminder.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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

    override suspend fun schedule(reminderId: Long, intervalMinutes: Int) {
        val inputData = Data.Builder()
            .putLong(ReminderNotificationWorker.KEY_REMINDER_ID, reminderId)
            .build()
        val request = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        ).setInputData(inputData).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "notification_reminder_$reminderId",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    override suspend fun cancel(reminderId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("notification_reminder_$reminderId")
    }
}
