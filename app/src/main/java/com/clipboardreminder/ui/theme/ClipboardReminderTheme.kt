package com.clipboardreminder.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.clipboardreminder.domain.model.ThemeMode

// Esquema de cores fixo para Android < 12 (One UI inspired)
private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1A73E8),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD3E3FD),
    secondary = androidx.compose.ui.graphics.Color(0xFF5F6368),
    background = androidx.compose.ui.graphics.Color(0xFFF8F9FA),
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color(0xFF202124),
    onSurface = androidx.compose.ui.graphics.Color(0xFF202124),
)

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF8AB4F8),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF1A1A2E),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF1A3A6B),
    secondary = androidx.compose.ui.graphics.Color(0xFF9AA0A6),
    background = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    surface = androidx.compose.ui.graphics.Color(0xFF2D2D2D),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE3E3E3),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE3E3E3),
)

@Composable
fun ClipboardReminderTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
