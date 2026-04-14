package com.clipboardreminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.clipboardreminder.data.local.dao.FieldDao
import com.clipboardreminder.data.local.dao.ReminderDao
import com.clipboardreminder.data.local.entity.FieldEntity
import com.clipboardreminder.data.local.entity.ReminderEntity

@Database(
    entities = [FieldEntity::class, ReminderEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fieldDao(): FieldDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        /** Migration 1 → 2: add color to fields and reminders, add updatedAt to reminders */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE fields ADD COLUMN color INTEGER")
                db.execSQL("ALTER TABLE reminders ADD COLUMN color INTEGER")
                db.execSQL("ALTER TABLE reminders ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }

        /** Migration 2 → 3: add isPinned to reminders */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
