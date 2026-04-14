package com.clipboardreminder.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.domain.model.ReminderUi
import com.clipboardreminder.domain.model.SortOrder
import com.clipboardreminder.domain.repository.ReminderRepository
import com.clipboardreminder.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderListUiState(
    val fieldId: Long = -1L,
    val fieldName: String = "",
    val reminders: List<ReminderUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val copySuccessId: Long? = null,
    val showDeleteConfirmation: Boolean = false,
    val reminderToDelete: ReminderUi? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.ALPHABETICAL,
    val filterColor: Int? = null,
    val showSortSheet: Boolean = false
)

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reminderRepository: ReminderRepository,
    private val createReminderUseCase: CreateReminderUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val copyReminderUseCase: CopyReminderUseCase,
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
    private val cancelNotificationUseCase: CancelNotificationUseCase,
    private val getMostUsedRemindersUseCase: GetMostUsedRemindersUseCase
) : ViewModel() {

    private val fieldId: Long = checkNotNull(savedStateHandle["fieldId"])
    private val fieldName: String = savedStateHandle["fieldName"] ?: ""
    private val isMostUsed: Boolean = savedStateHandle["isMostUsed"] ?: false

    private val _uiState = MutableStateFlow(
        ReminderListUiState(fieldId = fieldId, fieldName = fieldName)
    )
    val uiState: StateFlow<ReminderListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    // If it's the "most used" screen, default sort is MOST_USED. Otherwise ALPHABETICAL.
    private val _sortOrder = MutableStateFlow(if (isMostUsed) SortOrder.MOST_USED else SortOrder.ALPHABETICAL)
    private val _filterColor = MutableStateFlow<Int?>(null)

    init {
        val remindersFlow = if (isMostUsed) {
            getMostUsedRemindersUseCase()
        } else {
            reminderRepository.getRemindersForField(fieldId).map { reminders ->
                reminders.map { r ->
                    ReminderUi(
                        id = r.id, fieldId = r.fieldId, title = r.title, content = r.content,
                        usageCount = r.usageCount, notificationEnabled = r.notificationEnabled,
                        notificationIntervalMinutes = r.notificationIntervalMinutes,
                        color = r.color, updatedAt = r.updatedAt,
                        isPinned = r.isPinned
                    )
                }
            }
        }

        combine(
            remindersFlow,
            _searchQuery,
            _sortOrder,
            _filterColor
        ) { reminders, query, sort, colorFilter ->
            var filtered = if (query.isBlank()) reminders
                           else reminders.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }

            filtered = when (colorFilter) {
                null -> filtered
                -1   -> filtered.filter { it.color == null }
                else -> filtered.filter { it.color == colorFilter }
            }

            filtered = when (sort) {
                SortOrder.ALPHABETICAL      -> filtered.sortedBy { it.title.lowercase() }
                SortOrder.ALPHABETICAL_DESC -> filtered.sortedByDescending { it.title.lowercase() }
                SortOrder.MOST_USED         -> filtered.sortedByDescending { it.usageCount }
                SortOrder.LAST_MODIFIED     -> filtered.sortedByDescending { it.updatedAt }
                else                        -> filtered.sortedBy { it.title.lowercase() }
            }

            filtered
        }.onEach { filteredReminders ->
            _uiState.update { it.copy(reminders = filteredReminders, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortOrder(sort: SortOrder) {
        _sortOrder.value = sort
        _uiState.update { it.copy(sortOrder = sort, showSortSheet = false) }
    }

    fun setFilterColor(color: Int?) {
        _filterColor.value = color
        _uiState.update { it.copy(filterColor = color) }
    }

    fun showSortSheet() = _uiState.update { it.copy(showSortSheet = true) }
    fun hideSortSheet() = _uiState.update { it.copy(showSortSheet = false) }

    fun createReminder(title: String, content: String, color: Int? = null) {
        // Para manter a assinatura existente, vamos atualizar via update (ou alterar UseCase).
        // A assinatura do CreateReminderUseCase não tem color. Vamos usar update temporariamente ou o banco salva com null.
        viewModelScope.launch {
            createReminderUseCase(fieldId, title, content).onSuccess { createdReminder ->
                if (color != null) {
                    updateReminderUseCase(createdReminder.copy(color = color))
                }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            updateReminderUseCase(reminder).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun togglePinReminder(reminderId: Long) {
        viewModelScope.launch {
            val reminder = uiState.value.reminders.find { it.id == reminderId } ?: return@launch
            val updatedReminder = Reminder(
                id = reminder.id,
                fieldId = reminder.fieldId,
                title = reminder.title,
                content = reminder.content,
                usageCount = reminder.usageCount,
                notificationEnabled = reminder.notificationEnabled,
                notificationIntervalMinutes = reminder.notificationIntervalMinutes,
                color = reminder.color,
                updatedAt = reminder.updatedAt,
                isPinned = !reminder.isPinned
            )
            updateReminderUseCase(updatedReminder).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun requestDeleteReminder(reminderUi: ReminderUi) {
        _uiState.update { it.copy(showDeleteConfirmation = true, reminderToDelete = reminderUi) }
    }

    fun confirmDeleteReminder() {
        val reminderId = _uiState.value.reminderToDelete?.id ?: return
        _uiState.update { it.copy(showDeleteConfirmation = false, reminderToDelete = null) }
        viewModelScope.launch {
            deleteReminderUseCase(reminderId).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun cancelDeleteReminder() {
        _uiState.update { it.copy(showDeleteConfirmation = false, reminderToDelete = null) }
    }

    fun copyReminder(reminderId: Long) {
        viewModelScope.launch {
            copyReminderUseCase(reminderId)
                .onSuccess { _uiState.update { it.copy(copySuccessId = reminderId) } }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun clearCopySuccess() {
        _uiState.update { it.copy(copySuccessId = null) }
    }

    fun scheduleNotification(reminderId: Long, intervalMinutes: Int) {
        viewModelScope.launch {
            scheduleNotificationUseCase(reminderId, intervalMinutes).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun cancelNotification(reminderId: Long) {
        viewModelScope.launch {
            cancelNotificationUseCase(reminderId).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
