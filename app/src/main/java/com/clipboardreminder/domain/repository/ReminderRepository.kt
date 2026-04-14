package com.clipboardreminder.domain.repository

import com.clipboardreminder.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getRemindersForField(fieldId: Long): Flow<List<Reminder>>
    fun getMostUsed(limit: Int = 20): Flow<List<Reminder>>
    suspend fun createReminder(reminder: Reminder): Reminder
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(id: Long)
    suspend fun incrementUsageCount(id: Long)
    fun searchReminders(query: String): Flow<List<Reminder>>
    suspend fun getReminderById(id: Long): Reminder?
    suspend fun getRemindersWithNotification(): List<Reminder>
    fun getPinnedReminders(): Flow<List<Reminder>>
}
