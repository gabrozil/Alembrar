package com.clipboardreminder.notification

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.clipboardreminder.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COPY = "com.clipboardreminder.ACTION_COPY"
        const val ACTION_OPEN_POPUP = "com.clipboardreminder.ACTION_OPEN_POPUP"
        const val ACTION_DISMISS = "com.clipboardreminder.ACTION_DISMISS"
        const val EXTRA_REMINDER_ID = "reminder_id"
    }

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_COPY -> handleCopy(context, intent)
            ACTION_OPEN_POPUP -> handleOpenPopup(context)
            ACTION_DISMISS -> handleDismiss(context)
        }
    }

    private fun handleCopy(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId) ?: return@launch
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("reminder_content", reminder.content)
                clipboard.setPrimaryClip(clip)
                reminderRepository.incrementUsageCount(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleOpenPopup(context: Context) {
        val openIntent = Intent(context, com.clipboardreminder.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                   Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Hint for multi-tasking / popup mode
            // On some devices, adding specific categories or flags helps
        }
        context.startActivity(openIntent)
        
        // Collapse notification panel
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context.sendBroadcast(it)
    }

    private fun handleDismiss(context: Context) {
        val stopIntent = Intent(context, PersistentNotificationService::class.java)
        context.stopService(stopIntent)
    }
}
