package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.repository.NotificationRepository
import com.clipboardreminder.domain.repository.ReminderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancelNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: Long): Result<Unit> =
        runCatching {
            val reminder = reminderRepository.getReminderById(reminderId)
                ?: throw NoSuchElementException("Lembrete não encontrado")
            reminderRepository.updateReminder(
                reminder.copy(notificationEnabled = false, notificationIntervalMinutes = null)
            )
            notificationRepository.cancel(reminderId)
        }
}
