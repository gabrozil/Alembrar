package com.clipboardreminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.clipboardreminder.MainActivity
import com.clipboardreminder.domain.repository.ReminderRepository
import com.clipboardreminder.domain.validation.generateNotificationPreview
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val reminderRepository: ReminderRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_FIELD_ID = "field_id"
        const val CHANNEL_ID = "reminders_channel"
        const val CHANNEL_NAME = "Lembretes"
    }

    override suspend fun doWork(): Result {
        return try {
            val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
            if (reminderId == -1L) return Result.failure()

            val reminder = reminderRepository.getReminderById(reminderId) ?: return Result.failure()

            createNotificationChannel()

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("field_id", reminder.fieldId)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context, reminderId.toInt(), openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val copyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_COPY
                putExtra(NotificationActionReceiver.EXTRA_REMINDER_ID, reminderId)
            }
            val copyPendingIntent = PendingIntent.getBroadcast(
                context, reminderId.toInt(), copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val preview = generateNotificationPreview(reminder.content)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(reminder.title)
                .setContentText(preview)
                .setStyle(NotificationCompat.BigTextStyle().bigText(preview))
                .setContentIntent(openAppPendingIntent)
                .addAction(android.R.drawable.ic_menu_share, "Copiar", copyPendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(reminderId.toInt(), notification)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
