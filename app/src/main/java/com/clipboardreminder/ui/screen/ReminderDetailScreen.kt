package com.clipboardreminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.ui.viewmodel.ReminderListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    fieldId: Long,
    reminderId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: ReminderListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = reminderId != null
    val existingReminder = if (isEditing) uiState.reminders.find { it.id == reminderId } else null

    var title by remember(existingReminder) { mutableStateOf(existingReminder?.title ?: "") }
    var content by remember(existingReminder) { mutableStateOf(existingReminder?.content ?: "") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Lembrete" else "Novo Lembrete") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("Título") },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    contentError = null
                },
                label = { Text("Conteúdo") },
                isError = contentError != null,
                supportingText = contentError?.let { { Text(it) } },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    var valid = true
                    if (title.isBlank()) {
                        titleError = "O título não pode ser vazio"
                        valid = false
                    } else if (title.length > 100) {
                        titleError = "O título deve ter no máximo 100 caracteres"
                        valid = false
                    }
                    if (content.isBlank()) {
                        contentError = "O conteúdo não pode ser vazio"
                        valid = false
                    } else if (content.length > 5000) {
                        contentError = "O conteúdo deve ter no máximo 5000 caracteres"
                        valid = false
                    }
                    if (valid) {
                        if (isEditing && existingReminder != null) {
                            viewModel.updateReminder(
                                Reminder(
                                    id = existingReminder.id,
                                    fieldId = existingReminder.fieldId,
                                    title = title.trim(),
                                    content = content.trim(),
                                    usageCount = existingReminder.usageCount,
                                    notificationEnabled = existingReminder.notificationEnabled,
                                    notificationIntervalMinutes = existingReminder.notificationIntervalMinutes
                                )
                            )
                        } else {
                            viewModel.createReminder(title.trim(), content.trim())
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        }
    }
}
