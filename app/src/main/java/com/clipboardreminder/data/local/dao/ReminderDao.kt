package com.clipboardreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.clipboardreminder.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE fieldId = :fieldId ORDER BY title ASC")
    fun getByField(fieldId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY usageCount DESC LIMIT :limit")
    fun getMostUsed(limit: Int): Flow<List<ReminderEntity>>

    @Query("""
        SELECT * FROM reminders
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        COLLATE NOCASE
    """)
    fun search(query: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE notificationEnabled = 1")
    suspend fun getRemindersWithNotification(): List<ReminderEntity>

    @Query("UPDATE reminders SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Transaction
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
