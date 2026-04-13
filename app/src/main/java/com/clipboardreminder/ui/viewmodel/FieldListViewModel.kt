package com.clipboardreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipboardreminder.domain.model.Field
import com.clipboardreminder.domain.model.FieldUi
import com.clipboardreminder.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FieldListUiState(
    val fields: List<FieldUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val fieldToDelete: FieldUi? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class FieldListViewModel @Inject constructor(
    private val getOrderedFieldsUseCase: GetOrderedFieldsUseCase,
    private val createFieldUseCase: CreateFieldUseCase,
    private val updateFieldUseCase: UpdateFieldUseCase,
    private val deleteFieldUseCase: DeleteFieldUseCase,
    private val togglePinFieldUseCase: TogglePinFieldUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FieldListUiState())
    val uiState: StateFlow<FieldListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        combine(
            getOrderedFieldsUseCase(),
            _searchQuery
        ) { fields, query ->
            if (query.isBlank()) {
                fields
            } else {
                fields.filter { it.name.contains(query, ignoreCase = true) }
            }
        }.onEach { filteredFields ->
            _uiState.update { it.copy(fields = filteredFields, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun createField(name: String) {
        viewModelScope.launch {
            createFieldUseCase(name).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun updateField(field: Field) {
        viewModelScope.launch {
            updateFieldUseCase(field).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun requestDeleteField(fieldUi: FieldUi) {
        _uiState.update { it.copy(showDeleteConfirmation = true, fieldToDelete = fieldUi) }
    }

    fun confirmDeleteField() {
        val fieldId = _uiState.value.fieldToDelete?.id ?: return
        _uiState.update { it.copy(showDeleteConfirmation = false, fieldToDelete = null) }
        viewModelScope.launch {
            deleteFieldUseCase(fieldId).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun cancelDeleteField() {
        _uiState.update { it.copy(showDeleteConfirmation = false, fieldToDelete = null) }
    }

    fun togglePin(fieldId: Long) {
        viewModelScope.launch {
            togglePinFieldUseCase(fieldId).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
