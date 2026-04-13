package com.clipboardreminder.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.domain.model.ReminderUi
import com.clipboardreminder.ui.util.rememberNotificationPermissionHandler
import com.clipboardreminder.ui.viewmodel.ReminderListViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    fieldId: Long,
    fieldName: String,
    isMostUsed: Boolean,
    onNavigateBack: () -> Unit,
    viewModel: ReminderListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<ReminderUi?>(null) }
    var notificationReminder by remember { mutableStateOf<ReminderUi?>(null) }
    var pendingNotificationReminder by remember { mutableStateOf<ReminderUi?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }

    val requestNotificationPermission = rememberNotificationPermissionHandler(
        onPermissionGranted = { notificationReminder = pendingNotificationReminder }
    )

    // Show snackbar on error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Show "Copiado!" snackbar on copy success
    LaunchedEffect(uiState.copySuccessId) {
        if (uiState.copySuccessId != null) {
            snackbarHostState.showSnackbar("Copiado!")
            viewModel.clearCopySuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Pesquisar lembretes...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(fieldName)
                    }
                },
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar busca")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isMostUsed) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Novo lembrete")
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.reminders, key = { it.id }) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        isCopied = uiState.copySuccessId == reminder.id,
                        onCopy = { viewModel.copyReminder(reminder.id) },
                        onEdit = { editingReminder = reminder },
                        onDelete = { viewModel.requestDeleteReminder(reminder) },
                        onToggleNotification = {
                            if (reminder.notificationEnabled) {
                                viewModel.cancelNotification(reminder.id)
                            } else {
                                pendingNotificationReminder = reminder
                                requestNotificationPermission()
                            }
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteReminder() },
            title = { Text("Excluir lembrete") },
            text = {
                Text("Deseja excluir o lembrete \"${uiState.reminderToDelete?.title}\"?")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteReminder() }) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteReminder() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Create dialog
    if (showCreateDialog) {
        ReminderDialog(
            title = "Novo lembrete",
            initialTitle = "",
            initialContent = "",
            onConfirm = { t, c ->
                viewModel.createReminder(t, c)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Edit dialog
    editingReminder?.let { reminder ->
        ReminderDialog(
            title = "Editar lembrete",
            initialTitle = reminder.title,
            initialContent = reminder.content,
            onConfirm = { t, c ->
                viewModel.updateReminder(
                    Reminder(
                        id = reminder.id,
                        fieldId = reminder.fieldId,
                        title = t,
                        content = c,
                        usageCount = reminder.usageCount,
                        notificationEnabled = reminder.notificationEnabled,
                        notificationIntervalMinutes = reminder.notificationIntervalMinutes
                    )
                )
                editingReminder = null
            },
            onDismiss = { editingReminder = null }
        )
    }

    // Notification config dialog
    notificationReminder?.let { reminder ->
        NotificationIntervalDialog(
            onConfirm = { intervalMinutes ->
                viewModel.scheduleNotification(reminder.id, intervalMinutes)
                notificationReminder = null
            },
            onDismiss = { notificationReminder = null }
        )
    }
}

@Composable
private fun ReminderItem(
    reminder: ReminderUi,
    isCopied: Boolean,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleNotification: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showCopyFeedback by remember { mutableStateOf(false) }

    // Animate copy button color for 1 second
    LaunchedEffect(isCopied) {
        if (isCopied) {
            showCopyFeedback = true
            delay(1000L)
            showCopyFeedback = false
        }
    }

    val copyIconColor by animateColorAsState(
        targetValue = if (showCopyFeedback)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "copyIconColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                // Notification bell icon
                if (reminder.notificationEnabled) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificação ativa",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                }

                // Copy button
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        tint = copyIconColor
                    )
                }

                // Context menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opções"
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir") },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (reminder.notificationEnabled)
                                        "Desativar notificação"
                                    else
                                        "Ativar notificação"
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleNotification()
                            }
                        )
                    }
                }
            }

            // Content preview (max 2 lines)
            Text(
                text = reminder.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ReminderDialog(
    title: String,
    initialTitle: String,
    initialContent: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var reminderTitle by remember { mutableStateOf(initialTitle) }
    var reminderContent by remember { mutableStateOf(initialContent) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = reminderTitle,
                    onValueChange = {
                        reminderTitle = it
                        titleError = null
                    },
                    label = { Text("Título") },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reminderContent,
                    onValueChange = {
                        reminderContent = it
                        contentError = null
                    },
                    label = { Text("Conteúdo") },
                    isError = contentError != null,
                    supportingText = contentError?.let { { Text(it) } },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var valid = true
                    if (reminderTitle.isBlank()) {
                        titleError = "O título não pode ser vazio"
                        valid = false
                    } else if (reminderTitle.length > 100) {
                        titleError = "O título deve ter no máximo 100 caracteres"
                        valid = false
                    }
                    if (reminderContent.isBlank()) {
                        contentError = "O conteúdo não pode ser vazio"
                        valid = false
                    } else if (reminderContent.length > 5000) {
                        contentError = "O conteúdo deve ter no máximo 5000 caracteres"
                        valid = false
                    }
                    if (valid) onConfirm(reminderTitle.trim(), reminderContent.trim())
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun NotificationIntervalDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val intervals = listOf(
        15 to "15 minutos",
        30 to "30 minutos",
        60 to "1 hora",
        120 to "2 horas",
        360 to "6 horas",
        720 to "12 horas",
        1440 to "24 horas"
    )
    var selectedInterval by remember { mutableStateOf(60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Intervalo de notificação") },
        text = {
            Column {
                intervals.forEach { (minutes, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedInterval == minutes,
                            onClick = { selectedInterval = minutes }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedInterval) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
