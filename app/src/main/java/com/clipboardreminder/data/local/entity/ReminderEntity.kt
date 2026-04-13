package com.clipboardreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = FieldEntity::class,
        parentColumns = ["id"],
        childColumns = ["fieldId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("fieldId")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fieldId: Long,
    val title: String,
    val content: String,
    val usageCount: Int = 0,
    val notificationEnabled: Boolean = false,
    val notificationIntervalMinutes: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
