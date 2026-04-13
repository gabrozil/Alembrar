package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.repository.NotificationRepository
import com.clipboardreminder.domain.repository.ReminderRepository
import com.clipboardreminder.domain.validation.validateNotificationInterval
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: Long, intervalMinutes: Int): Result<Unit> {
        if (!validateNotificationInterval(intervalMinutes)) {
            return Result.failure(IllegalArgumentException("Intervalo inválido: $intervalMinutes"))
        }
        return runCatching {
            val reminder = reminderRepository.getReminderById(reminderId)
                ?: throw NoSuchElementException("Lembrete não encontrado")
            reminderRepository.updateReminder(
                reminder.copy(
                    notificationEnabled = true,
                    notificationIntervalMinutes = intervalMinutes
                )
            )
            notificationRepository.schedule(reminderId, intervalMinutes)
        }
    }
}
