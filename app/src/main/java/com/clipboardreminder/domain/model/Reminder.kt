package com.clipboardreminder.domain.model

data class Reminder(
    val id: Long,
    val fieldId: Long,
    val title: String,
    val content: String,
    val usageCount: Int,
    val notificationEnabled: Boolean,
    val notificationIntervalMinutes: Int?,
    val color: Int? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
