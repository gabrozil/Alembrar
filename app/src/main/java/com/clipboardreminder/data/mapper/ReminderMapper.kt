package com.clipboardreminder.data.mapper

import com.clipboardreminder.data.local.entity.ReminderEntity
import com.clipboardreminder.domain.model.Reminder

fun ReminderEntity.toDomain() = Reminder(
    id = id,
    fieldId = fieldId,
    title = title,
    content = content,
    usageCount = usageCount,
    notificationEnabled = notificationEnabled,
    notificationIntervalMinutes = notificationIntervalMinutes,
    color = color,
    updatedAt = updatedAt,
    isPinned = isPinned
)

fun Reminder.toEntity() = ReminderEntity(
    id = id,
    fieldId = fieldId,
    title = title,
    content = content,
    usageCount = usageCount,
    notificationEnabled = notificationEnabled,
    notificationIntervalMinutes = notificationIntervalMinutes,
    color = color,
    updatedAt = updatedAt,
    isPinned = isPinned
)
