package com.clipboardreminder.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.clipboardreminder.domain.model.ThemeMode

// ── Paleta Alembrar ──────────────────────────────────────────────────────
val NavyDeep     = Color(0xFF0D1B2A)
val NavyMid      = Color(0xFF1B2F45)
val NavyLight    = Color(0xFF243B55)
val GoldAccent   = Color(0xFFF4A300)
val GoldLight    = Color(0xFFFFCA28)
val CreamWhite   = Color(0xFFF5F0E8)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark  = Color(0xFF12202F)
val CardDark     = Color(0xFF1A2D40)
val OnDark       = Color(0xFFE8E8E8)
val ErrorRed     = Color(0xFFCF6679)

private val LightColorScheme = lightColorScheme(
    primary          = NavyDeep,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD0E8FF),
    onPrimaryContainer = NavyDeep,
    secondary        = GoldAccent,
    onSecondary      = NavyDeep,
    secondaryContainer = Color(0xFFFFF3CD),
    onSecondaryContainer = Color(0xFF4A3000),
    background       = CreamWhite,
    onBackground     = NavyDeep,
    surface          = SurfaceLight,
    onSurface        = NavyDeep,
    surfaceVariant   = Color(0xFFECF3FB),
    onSurfaceVariant = Color(0xFF4A5568),
    outline          = Color(0xFFB0C4D8),
    error            = ErrorRed,
)

private val DarkColorScheme = darkColorScheme(
    primary          = GoldAccent,
    onPrimary        = NavyDeep,
    primaryContainer = NavyLight,
    onPrimaryContainer = GoldLight,
    secondary        = GoldLight,
    onSecondary      = NavyDeep,
    secondaryContainer = Color(0xFF3D2C00),
    onSecondaryContainer = GoldLight,
    background       = NavyDeep,
    onBackground     = OnDark,
    surface          = SurfaceDark,
    onSurface        = OnDark,
    surfaceVariant   = CardDark,
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline          = Color(0xFF2A3F55),
    error            = ErrorRed,
)

// ── Tipografia ───────────────────────────────────────────────────────────
// Usa fontes do sistema com fallback elegante (sem necessidade de asset)
val AlembrarTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.15).sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.1).sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.5).sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.25).sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.4).sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.1).sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.5).sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.5).sp
    ),
)

// ── Tema Principal ────────────────────────────────────────────────────────
@Composable
fun ClipboardReminderTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Usamos nosso esquema de cores personalizado (não dynamic) para manter identidade visual
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AlembrarTypography,
        content     = content
    )
}
