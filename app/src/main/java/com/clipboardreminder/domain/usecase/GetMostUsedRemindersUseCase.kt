package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.model.ReminderUi
import com.clipboardreminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetMostUsedRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<ReminderUi>> =
        reminderRepository.getMostUsed(limit).map { reminders ->
            reminders.map { reminder ->
                ReminderUi(
                    id = reminder.id,
                    fieldId = reminder.fieldId,
                    title = reminder.title,
                    content = reminder.content,
                    usageCount = reminder.usageCount,
                    notificationEnabled = reminder.notificationEnabled,
                    notificationIntervalMinutes = reminder.notificationIntervalMinutes
                )
            }
        }
}
