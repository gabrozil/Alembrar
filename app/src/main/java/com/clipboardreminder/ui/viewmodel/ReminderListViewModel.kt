package com.clipboardreminder.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipboardreminder.domain.model.FieldUi
import com.clipboardreminder.domain.model.Reminder
import com.clipboardreminder.domain.model.ReminderUi
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
    val searchQuery: String = ""
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

    init {
        val remindersFlow = if (isMostUsed) {
            getMostUsedRemindersUseCase()
        } else {
            reminderRepository.getRemindersForField(fieldId).map { reminders ->
                reminders.map { r ->
                    ReminderUi(r.id, r.fieldId, r.title, r.content, r.usageCount, r.notificationEnabled, r.notificationIntervalMinutes)
                }
            }
        }

        combine(
            remindersFlow,
            _searchQuery
        ) { reminders, query ->
            if (query.isBlank()) {
                reminders
            } else {
                reminders.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.content.contains(query, ignoreCase = true) 
                }
            }
        }.onEach { filteredReminders ->
            _uiState.update { it.copy(reminders = filteredReminders, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun createReminder(title: String, content: String) {
        viewModelScope.launch {
            createReminderUseCase(fieldId, title, content).onFailure { e ->
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
