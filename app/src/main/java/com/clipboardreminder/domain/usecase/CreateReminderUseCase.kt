package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.domain.repository.ReminderRepository
import com.clipboardreminder.domain.validation.ValidationResult
import com.clipboardreminder.domain.validation.validateReminder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateReminderUseCase @Inject constructor(private val reminderRepository: ReminderRepository) {
    suspend operator fun invoke(fieldId: Long, title: String, content: String): Result<Reminder> {
        val validation = validateReminder(title, content)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.reason))
        }
        val reminder = Reminder(
            id = 0,
            fieldId = fieldId,
            title = title.trim(),
            content = content.trim(),
            usageCount = 0,
            notificationEnabled = false,
            notificationIntervalMinutes = null
        )
        return runCatching { reminderRepository.createReminder(reminder) }
    }
}
