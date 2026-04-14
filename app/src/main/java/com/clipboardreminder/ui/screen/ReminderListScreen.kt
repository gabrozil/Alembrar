package com.clipboardreminder.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.domain.model.ReminderUi
import com.clipboardreminder.ui.theme.GoldAccent
import com.clipboardreminder.ui.theme.GoldLight
import com.clipboardreminder.ui.theme.NavyDeep
import com.clipboardreminder.ui.util.rememberNotificationPermissionHandler
import com.clipboardreminder.ui.viewmodel.ReminderListViewModel
import com.clipboardreminder.ui.component.ColorPickerRow
import com.clipboardreminder.ui.component.ColorAccentStrip
import com.clipboardreminder.ui.component.SortFilterSheet
import kotlinx.coroutines.delay

@Composable
fun ReminderItem(
    reminder: ReminderUi,
    isCopied: Boolean,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleNotification: () -> Unit,
    onTogglePin: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showCopyFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) { showCopyFeedback = true; delay(1200L); showCopyFeedback = false }
    }

    val copyIconColor by animateColorAsState(
        targetValue = if (showCopyFeedback) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300), label = "copyIconColor"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 3.dp, label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onCopy
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            ColorAccentStrip(color = reminder.color)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification badge
                if (reminder.notificationEnabled) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Copy button with animation
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showCopyFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        tint = copyIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text(if (reminder.notificationEnabled) "Desativar notificação" else "Ativar notificação") },
                            leadingIcon = { Icon(Icons.Default.Notifications, null) },
                            onClick = { menuExpanded = false; onToggleNotification() }
                        )
                        DropdownMenuItem(
                            text = { Text(if (reminder.isPinned) "Desafixar" else "Fixar na Bolha") },
                            leadingIcon = { Icon(if (reminder.isPinned) Icons.Default.PushPin else Icons.Default.PushPin, null) }, // Using PushPin for both, or could use Outlined
                            onClick = { menuExpanded = false; onTogglePin() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }

            // Content preview
            Spacer(Modifier.height(6.dp))
            Text(
                text = reminder.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                )
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────
@Composable
fun EmptyRemindersPlaceholder(fieldName: String, isMostUsed: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(GoldAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isMostUsed) "⭐" else "📝", fontSize = 36.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            if (isMostUsed) "Nenhum lembrete utilizado ainda"
            else "Nenhum lembrete em \"$fieldName\"",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (!isMostUsed) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Toque em + para adicionar um lembrete",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Create/Edit Dialog ──────────────────────────────────────────────────────
@Composable
fun ReminderDialog(
    title: String,
    initialTitle: String,
    initialContent: String,
    initialColor: Int?,
    onConfirm: (String, String, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var reminderTitle by remember { mutableStateOf(initialTitle) }
    var reminderContent by remember { mutableStateOf(initialContent) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldAccent,
        focusedLabelColor = GoldAccent,
        cursorColor = GoldAccent
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = reminderTitle,
                    onValueChange = { reminderTitle = it; titleError = null },
                    label = { Text("Título") },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    singleLine = true,
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reminderContent,
                    onValueChange = { reminderContent = it; contentError = null },
                    label = { Text("Conteúdo") },
                    isError = contentError != null,
                    supportingText = contentError?.let { { Text(it) } },
                    minLines = 3,
                    maxLines = 8,
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ColorPickerRow(selectedColor = selectedColor, onColorSelected = { selectedColor = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var valid = true
                    if (reminderTitle.isBlank()) { titleError = "O título não pode ser vazio"; valid = false }
                    else if (reminderTitle.length > 100) { titleError = "Máximo de 100 caracteres"; valid = false }
                    if (reminderContent.isBlank()) { contentError = "O conteúdo não pode ser vazio"; valid = false }
                    else if (reminderContent.length > 5000) { contentError = "Máximo de 5000 caracteres"; valid = false }
                    if (valid) onConfirm(reminderTitle.trim(), reminderContent.trim(), selectedColor)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Confirmar", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ── Notification Dialog ───────────────────────────────────────────────────
@Composable
fun NotificationIntervalDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val intervals = listOf(
        15 to "15 minutos", 30 to "30 minutos", 60 to "1 hora",
        120 to "2 horas", 360 to "6 horas", 720 to "12 horas", 1440 to "24 horas"
    )
    var selected by remember { mutableStateOf(60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Intervalo de notificação", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                intervals.forEach { (minutes, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selected = minutes }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selected == minutes,
                            onClick = { selected = minutes },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selected) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Confirmar", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
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

    val fabRotation by animateFloatAsState(
        targetValue = if (showCreateDialog) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fabRotation"
    )

    val requestNotificationPermission = rememberNotificationPermissionHandler(
        onPermissionGranted = { notificationReminder = pendingNotificationReminder }
    )

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.copySuccessId) {
        if (uiState.copySuccessId != null) {
            snackbarHostState.showSnackbar("✓ Copiado!")
            viewModel.clearCopySuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = isSearchActive,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                        label = "reminderTopBarTitle"
                    ) { searchActive ->
                        if (searchActive) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = { Text("Pesquisar lembretes...", style = MaterialTheme.typography.bodyMedium) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                        } else {
                            Text(
                                text = fieldName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = if (isSearchActive) {
                        { isSearchActive = false; viewModel.onSearchQueryChange("") }
                    } else onNavigateBack) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close
                                          else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(visible = !isSearchActive) {
                        Row {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            }
                            IconButton(onClick = { viewModel.showSortSheet() }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filtrar e Ordenar")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!isMostUsed) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyDeep,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Novo lembrete",
                        modifier = Modifier.rotate(fabRotation)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = GoldAccent) }
        } else if (uiState.reminders.isEmpty() && uiState.searchQuery.isEmpty()) {
            EmptyRemindersPlaceholder(fieldName = fieldName, isMostUsed = isMostUsed)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.reminders, key = { it.id }) { reminder ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                    ) {
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
                            },
                            onTogglePin = { viewModel.togglePinReminder(reminder.id) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Delete confirmation
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteReminder() },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Excluir lembrete") },
            text = { Text("Deseja excluir \"${uiState.reminderToDelete?.title}\"?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteReminder() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteReminder() }) { Text("Cancelar") }
            }
        )
    }

    // Create dialog
    if (showCreateDialog) {
        ReminderDialog(
            title = "Novo lembrete",
            initialTitle = "",
            initialContent = "",
            initialColor = null,
            onConfirm = { t, c, color ->
                viewModel.createReminder(t, c, color)
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
            initialColor = reminder.color,
            onConfirm = { t, c, color ->
                viewModel.updateReminder(
                    Reminder(
                        id = reminder.id, fieldId = reminder.fieldId,
                        title = t, content = c,
                        usageCount = reminder.usageCount,
                        notificationEnabled = reminder.notificationEnabled,
                        notificationIntervalMinutes = reminder.notificationIntervalMinutes,
                        color = color,
                        updatedAt = reminder.updatedAt,
                        isPinned = reminder.isPinned
                    )
                )
                editingReminder = null
            },
            onDismiss = { editingReminder = null }
        )
    }

    // Notification dialog
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

// ── Reminder Card ─────────────────────────────────────────────────────────
