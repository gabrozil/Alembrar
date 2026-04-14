package com.clipboardreminder.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
        const val CHANNEL_NAME = "Lembretes Alembrar"
        private const val TAG = "ReminderNotifWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
            Log.d(TAG, "doWork chamado para reminderId=$reminderId")

            if (reminderId == -1L) {
                Log.e(TAG, "reminderId inválido, abortando")
                return Result.failure()
            }

            val reminder = reminderRepository.getReminderById(reminderId)
            if (reminder == null) {
                Log.e(TAG, "Lembrete $reminderId não encontrado, abortando")
                return Result.failure()
            }

            if (!reminder.notificationEnabled) {
                Log.d(TAG, "Notificação desativada para $reminderId, cancelando worker")
                return Result.success()
            }

            // Verify notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    Log.w(TAG, "Permissão POST_NOTIFICATIONS negada, não enviando notificação")
                    return Result.success() // Don't retry — user hasn't granted permission
                }
            }

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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            NotificationManagerCompat.from(context).notify(reminderId.toInt(), notification)
            Log.d(TAG, "Notificação enviada para $reminderId: ${reminder.title}")

            Result.success()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException ao enviar notificação: ${e.message}")
            Result.success() // Don't retry on permission issues
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificações periódicas de lembretes do Alembrar"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
