package com.clipboardreminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clipboardreminder.domain.model.ThemeMode
import com.clipboardreminder.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Tema",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ThemeOption(
                label = "Seguir sistema",
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.updateThemeMode(ThemeMode.SYSTEM) }
            )
            ThemeOption(
                label = "Claro",
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.updateThemeMode(ThemeMode.LIGHT) }
            )
            ThemeOption(
                label = "Escuro",
                selected = themeMode == ThemeMode.DARK,
                onClick = { viewModel.updateThemeMode(ThemeMode.DARK) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Notificações",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val persistentEnabled by viewModel.persistentNotificationEnabled.collectAsState()

            ListItem(
                headlineContent = { Text("Notificação Fixa") },
                supportingContent = { Text("Mantém um atalho fixo na barra de notificações para acesso rápido.") },
                trailingContent = {
                    Switch(
                        checked = persistentEnabled,
                        onCheckedChange = { viewModel.updatePersistentNotification(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Atualizações",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val updateState by viewModel.updateState.collectAsState()
            
            ListItem(
                headlineContent = { Text("Versão do App") },
                supportingContent = { Text("${com.clipboardreminder.BuildConfig.VERSION_NAME} (${com.clipboardreminder.BuildConfig.VERSION_CODE})") },
                trailingContent = {
                    Button(
                        onClick = { viewModel.checkForUpdates() },
                        enabled = updateState !is com.clipboardreminder.domain.UpdateState.Checking && 
                                  updateState !is com.clipboardreminder.domain.UpdateState.Downloading
                    ) {
                        Text("Verificar")
                    }
                }
            )

            // Handle Update States
            when (val state = updateState) {
                is com.clipboardreminder.domain.UpdateState.Checking -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is com.clipboardreminder.domain.UpdateState.UpdateAvailable -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetUpdateState() },
                        title = { Text("Atualização Disponível") },
                        text = { Text("Uma nova versão (${state.info.versionName}) está disponível. Deseja baixar?") },
                        confirmButton = {
                            TextButton(onClick = { viewModel.downloadAndInstall(state.info) }) {
                                Text("Baixar e Instalar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.resetUpdateState() }) {
                                Text("Agora não")
                            }
                        }
                    )
                }
                is com.clipboardreminder.domain.UpdateState.Downloading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Baixando atualização: ${(state.progress * 100).toInt()}%")
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
                is com.clipboardreminder.domain.UpdateState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                is com.clipboardreminder.domain.UpdateState.NoUpdateAvailable -> {
                    Text(
                        text = "O app está atualizado",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
