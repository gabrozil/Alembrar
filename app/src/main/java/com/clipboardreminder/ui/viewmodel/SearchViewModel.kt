package com.clipboardreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipboardreminder.domain.model.ReminderUi
import com.clipboardreminder.domain.usecase.CopyReminderUseCase
import com.clipboardreminder.domain.usecase.SearchRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<ReminderUi> = emptyList(),
    val isEmpty: Boolean = false,
    val isSearching: Boolean = false,
    val copySuccessId: Long? = null,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRemindersUseCase: SearchRemindersUseCase,
    private val copyReminderUseCase: CopyReminderUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(300L)
                .flatMapLatest { query ->
                    _uiState.update { it.copy(query = query, isSearching = query.isNotBlank()) }
                    searchRemindersUseCase(query)
                }
                .collect { results ->
                    _uiState.update { state ->
                        state.copy(
                            results = results,
                            isEmpty = state.query.isNotBlank() && results.isEmpty(),
                            isSearching = false
                        )
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _query.value = query
        _uiState.update { it.copy(query = query, isSearching = query.isNotBlank()) }
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isEmpty = false, isSearching = false) }
        }
    }

    fun copyReminder(reminderId: Long) {
        viewModelScope.launch {
            copyReminderUseCase(reminderId)
                .onSuccess { _uiState.update { it.copy(copySuccessId = reminderId) } }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun clearCopySuccess() { _uiState.update { it.copy(copySuccessId = null) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
}
