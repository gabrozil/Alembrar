package com.clipboardreminder.domain.repository

import com.clipboardreminder.domain.model.AppPreferences
import com.clipboardreminder.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun getPreferences(): Flow<AppPreferences>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updatePersistentNotification(enabled: Boolean)
}
