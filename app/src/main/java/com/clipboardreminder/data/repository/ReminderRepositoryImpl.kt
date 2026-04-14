package com.clipboardreminder.data.repository

import com.clipboardreminder.data.local.dao.ReminderDao
import com.clipboardreminder.data.mapper.toDomain
import com.clipboardreminder.data.mapper.toEntity
import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getRemindersForField(fieldId: Long): Flow<List<Reminder>> =
        reminderDao.getByField(fieldId).map { entities -> entities.map { it.toDomain() } }

    override fun getMostUsed(limit: Int): Flow<List<Reminder>> =
        reminderDao.getMostUsed(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun createReminder(reminder: Reminder): Reminder {
        val id = reminderDao.insert(reminder.toEntity())
        return reminder.copy(id = id)
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.update(reminder.toEntity())
    }

    override suspend fun deleteReminder(id: Long) {
        reminderDao.deleteById(id)
    }

    override suspend fun incrementUsageCount(id: Long) {
        reminderDao.incrementUsage(id)
    }

    override fun searchReminders(query: String): Flow<List<Reminder>> =
        reminderDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getReminderById(id: Long): Reminder? =
        reminderDao.getReminderById(id)?.toDomain()

    override suspend fun getRemindersWithNotification(): List<Reminder> =
        reminderDao.getRemindersWithNotification().map { it.toDomain() }

    override fun getPinnedReminders(): Flow<List<Reminder>> =
        reminderDao.getPinned().map { entities -> entities.map { it.toDomain() } }
}
