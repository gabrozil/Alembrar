package com.clipboardreminder.domain.usecase

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.clipboardreminder.domain.repository.ReminderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CopyReminderUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: Long): Result<Unit> {
        val reminder = reminderRepository.getReminderById(reminderId)
            ?: return Result.failure(NoSuchElementException("Lembrete não encontrado"))

        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("reminder_content", reminder.content)
            clipboard.setPrimaryClip(clip)
            reminderRepository.incrementUsageCount(reminderId)
            Result.success(Unit)
        } catch (e: Exception) {
            // Não incrementa o contador se a cópia falhar
            Result.failure(e)
        }
    }
}
