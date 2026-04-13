package com.clipboardreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.clipboardreminder.ui.navigation.AppNavigation
import com.clipboardreminder.ui.theme.ClipboardReminderTheme
import com.clipboardreminder.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val navController = rememberNavController()

            ClipboardReminderTheme(themeMode = themeMode) {
                if (isPipMode) {
                    // PiP mode: show compact overlay
                    Surface(modifier = Modifier.fillMaxSize()) {
                        // PiP content is handled by PipOverlayContent
                        // For now, show a minimal surface
                    }
                } else {
                    AppNavigation(navController = navController)
                }
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Auto-enter PiP when user leaves app (optional)
        // enterPictureInPictureMode(PictureInPictureParams.Builder().build())
    }
}
