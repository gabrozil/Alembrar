package com.clipboardreminder.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.clipboardreminder.domain.model.AppPreferences
import com.clipboardreminder.domain.model.ThemeMode
import com.clipboardreminder.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreferencesRepository {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val persistentNotificationKey = booleanPreferencesKey("persistent_notification")

    override fun getPreferences(): Flow<AppPreferences> =
        dataStore.data.map { prefs ->
            val themeMode = prefs[themeModeKey]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM
            val persistentNotificationEnabled = prefs[persistentNotificationKey] ?: false
            AppPreferences(
                themeMode = themeMode,
                persistentNotificationEnabled = persistentNotificationEnabled
            )
        }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[themeModeKey] = themeMode.name
        }
    }

    override suspend fun updatePersistentNotification(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[persistentNotificationKey] = enabled
        }
    }
}
