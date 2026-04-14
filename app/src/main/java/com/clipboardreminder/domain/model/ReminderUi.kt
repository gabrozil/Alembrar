package com.clipboardreminder.domain.model

data class ReminderUi(
    val id: Long,
    val fieldId: Long,
    val title: String,
    val content: String,
    val usageCount: Int,
    val notificationEnabled: Boolean,
    val notificationIntervalMinutes: Int?,
    val color: Int? = null,
    val updatedAt: Long = 0L,
    val isPinned: Boolean = false
)
