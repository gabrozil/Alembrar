package com.clipboardreminder.domain.model

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val persistentNotificationEnabled: Boolean = false,
    val floatingBubbleEnabled: Boolean = false
)
