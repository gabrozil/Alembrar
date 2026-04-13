package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.repository.ReminderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteReminderUseCase @Inject constructor(private val reminderRepository: ReminderRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        runCatching { reminderRepository.deleteReminder(id) }
}
