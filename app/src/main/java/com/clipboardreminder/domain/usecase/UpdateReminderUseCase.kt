package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.domain.repository.ReminderRepository
import com.clipboardreminder.domain.validation.ValidationResult
import com.clipboardreminder.domain.validation.validateReminder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateReminderUseCase @Inject constructor(private val reminderRepository: ReminderRepository) {
    suspend operator fun invoke(reminder: Reminder): Result<Unit> {
        val validation = validateReminder(reminder.title, reminder.content)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.reason))
        }
        return runCatching {
            reminderRepository.updateReminder(
                reminder.copy(
                    title = reminder.title.trim(),
                    content = reminder.content.trim(),
                    updatedAt = System.currentTimeMillis() // Always update the timestamp on modification
                )
            )
        }
    }
}
