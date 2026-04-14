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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
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
import com.clipboardreminder.domain.model.Field
import com.clipboardreminder.domain.model.FieldUi
import com.clipboardreminder.domain.model.MOST_USED_FIELD_ID
import com.clipboardreminder.ui.theme.GoldAccent
import com.clipboardreminder.ui.theme.GoldLight
import com.clipboardreminder.ui.theme.NavyDeep
import com.clipboardreminder.ui.theme.NavyMid
import com.clipboardreminder.ui.viewmodel.FieldListViewModel
import com.clipboardreminder.ui.component.ColorPickerRow
import com.clipboardreminder.ui.component.ColorAccentStrip
import com.clipboardreminder.ui.component.SortFilterSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldListScreen(
    onNavigateToReminders: (fieldId: Long, fieldName: String, isMostUsed: Boolean) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: FieldListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingField by remember { mutableStateOf<FieldUi?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }

    // FAB rotation animation
    val fabRotation by animateFloatAsState(
        targetValue = if (fabExpanded) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fabRotation"
    )

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            AlembrarTopBar(
                isSearchActive = isSearchActive,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onSearchToggle = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) viewModel.onSearchQueryChange("")
                },
                onNavigateToSettings = onNavigateToSettings,
                onOpenSortFilter = { viewModel.showSortSheet() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    fabExpanded = !fabExpanded
                    showCreateDialog = true
                },
                containerColor = GoldAccent,
                contentColor = NavyDeep,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Novo campo",
                    modifier = Modifier.rotate(fabRotation)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            } else if (uiState.fields.isEmpty() && uiState.searchQuery.isEmpty()) {
                EmptyFieldsPlaceholder()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.fields, key = { it.id }) { field ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                        ) {
                            FieldItem(
                                field = field,
                                onFieldClick = { onNavigateToReminders(field.id, field.name, field.isMostUsed) },
                                onTogglePin = { viewModel.togglePin(field.id) },
                                onEdit = { editingField = field },
                                onDelete = { viewModel.requestDeleteField(field) }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (uiState.showSortSheet) {
        SortFilterSheet(
            sortOrder = uiState.sortOrder,
            onSortChange = { viewModel.setSortOrder(it) },
            filterColor = uiState.filterColor,
            onFilterColorChange = { viewModel.setFilterColor(it) },
            onDismissRequest = { viewModel.hideSortSheet() }
        )
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteField() },
            title = { Text("Excluir campo") },
            text = { Text("Deseja excluir \"${uiState.fieldToDelete?.name}\"? Todos os lembretes serão removidos.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteField() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteField() }) { Text("Cancelar") }
            }
        )
    }

    if (showCreateDialog) {
        FieldDialog(
            title = "Novo campo",
            initialName = "",
            initialColor = null,
            onConfirm = { name, color ->
                viewModel.createField(name, color)
                showCreateDialog = false
                fabExpanded = false
            },
            onDismiss = { showCreateDialog = false; fabExpanded = false }
        )
    }

    editingField?.let { field ->
        FieldDialog(
            title = "Editar campo",
            initialName = field.name,
            initialColor = field.color,
            onConfirm = { name, color ->
                viewModel.updateField(Field(id = field.id, name = name, isPinned = field.isPinned, color = color))
                editingField = null
            },
            onDismiss = { editingField = null }
        )
    }
}

// ── Top Bar com Logo ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlembrarTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenSortFilter: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.clipboardreminder.ui.theme.NavyDeep

    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                },
                label = "topBarTitle"
            ) { searchActive ->
                if (searchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Pesquisar campos...", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Gold dot logo mark
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(GoldAccent, GoldLight))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                color = NavyDeep,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Alembrar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        },
        navigationIcon = {
            AnimatedVisibility(visible = isSearchActive) {
                IconButton(onClick = onSearchToggle) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar busca")
                }
            }
        },
        actions = {
            AnimatedVisibility(visible = !isSearchActive) {
                Row {
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onOpenSortFilter) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar e Ordenar")
                    }
                }
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Configurações")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

// ── Field Card ────────────────────────────────────────────────────────────
@Composable
private fun FieldItem(
    field: FieldUi,
    onFieldClick: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isMostUsed = field.isMostUsed

    // Card press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 3.dp,
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onFieldClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorAccentStrip(color = field.color)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left icon
            if (isMostUsed) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐", fontSize = 18.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (field.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = if (field.isPinned) GoldAccent
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = field.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isMostUsed) {
                    Text(
                        text = "Mais utilizados",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent
                    )
                }
            }

            if (!isMostUsed) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (field.isPinned) "Desafixar" else "Fixar") },
                            leadingIcon = { Icon(if (field.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, null) },
                            onClick = { menuExpanded = false; onTogglePin() }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
}

// ── Empty State ───────────────────────────────────────────────────────────
@Composable
private fun EmptyFieldsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
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
            Text("📁", fontSize = 36.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Nenhum campo ainda",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Toque no botão + para criar seu primeiro campo de lembretes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ── Field Dialog ──────────────────────────────────────────────────────────
@Composable
private fun FieldDialog(
    title: String,
    initialName: String,
    initialColor: Int?,
    onConfirm: (String, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nome do campo") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        focusedLabelColor = GoldAccent,
                        cursorColor = GoldAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                ColorPickerRow(selectedColor = selectedColor, onColorSelected = { selectedColor = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "O nome não pode ser vazio"
                        name.length > 50 -> error = "Máximo de 50 caracteres"
                        else -> onConfirm(name.trim(), selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Confirmar", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
