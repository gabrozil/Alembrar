package com.clipboardreminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.clipboardreminder.data.local.dao.FieldDao
import com.clipboardreminder.data.local.dao.ReminderDao
import com.clipboardreminder.data.local.entity.FieldEntity
import com.clipboardreminder.data.local.entity.ReminderEntity

@Database(
    entities = [FieldEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fieldDao(): FieldDao
    abstract fun reminderDao(): ReminderDao
}
