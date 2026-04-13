package com.clipboardreminder.domain.validation

val VALID_NOTIFICATION_INTERVALS = setOf(15, 30, 60, 120, 360, 720, 1440)

fun validateFieldName(name: String): Boolean = name.length in 1..50

fun validateReminderTitle(title: String): Boolean = title.length in 1..100

fun validateReminderContent(content: String): Boolean = content.length in 1..5000

fun validateNotificationInterval(minutes: Int): Boolean = minutes in VALID_NOTIFICATION_INTERVALS

fun generateNotificationPreview(content: String): String =
    if (content.length <= 100) content else content.take(100)
