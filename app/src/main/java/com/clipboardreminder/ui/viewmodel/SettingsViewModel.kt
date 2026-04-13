package com.clipboardreminder.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipboardreminder.notification.PersistentNotificationService
import com.clipboardreminder.domain.UpdateManager
import com.clipboardreminder.domain.UpdateState
import com.clipboardreminder.domain.model.ThemeMode
import com.clipboardreminder.domain.model.UpdateInfo
import com.clipboardreminder.domain.repository.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val updateManager: UpdateManager,
    private val context: android.app.Application
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = appPreferencesRepository
        .getPreferences()
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    val persistentNotificationEnabled: StateFlow<Boolean> = appPreferencesRepository
        .getPreferences()
        .map { it.persistentNotificationEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val updateState: StateFlow<UpdateState> = updateManager.updateState

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            appPreferencesRepository.updateThemeMode(themeMode)
        }
    }

    fun updatePersistentNotification(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.updatePersistentNotification(enabled)
            val intent = Intent(context, PersistentNotificationService::class.java)
            if (enabled) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            updateManager.checkForUpdates()
        }
    }

    fun downloadAndInstall(info: UpdateInfo) {
        viewModelScope.launch {
            updateManager.downloadAndInstall(info)
        }
    }

    fun resetUpdateState() {
        updateManager.resetState()
    }
}
