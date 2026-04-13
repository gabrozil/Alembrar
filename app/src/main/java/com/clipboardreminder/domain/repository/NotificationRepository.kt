package com.clipboardreminder.domain.repository

interface NotificationRepository {
    suspend fun schedule(reminderId: Long, intervalMinutes: Int)
    suspend fun cancel(reminderId: Long)
}
