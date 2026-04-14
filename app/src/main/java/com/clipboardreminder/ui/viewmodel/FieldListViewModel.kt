package com.clipboardreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipboardreminder.domain.model.Field
import com.clipboardreminder.domain.model.FieldUi
import com.clipboardreminder.domain.model.SortOrder
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
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.ALPHABETICAL,
    val filterColor: Int? = null, // null = show all colors
    val showSortSheet: Boolean = false
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
    private val _sortOrder = MutableStateFlow(SortOrder.ALPHABETICAL)
    private val _filterColor = MutableStateFlow<Int?>(null)

    init {
        combine(
            getOrderedFieldsUseCase(),
            _searchQuery,
            _sortOrder,
            _filterColor
        ) { fields, query, sort, colorFilter ->
            val mostUsed = fields.firstOrNull { it.isMostUsed }
            val regularFields = fields.filter { !it.isMostUsed }

            var filtered = if (query.isBlank()) regularFields
                          else regularFields.filter { it.name.contains(query, ignoreCase = true) }

            // Color filter: -1 means show only "no color"
            filtered = when (colorFilter) {
                null -> filtered           // all
                -1   -> filtered.filter { it.color == null }
                else -> filtered.filter { it.color == colorFilter }
            }

            // Sort (pinned items always float to top)
            filtered = when (sort) {
                SortOrder.ALPHABETICAL      -> filtered.sortedWith(compareByDescending<FieldUi> { it.isPinned }.thenBy { it.name.lowercase() })
                SortOrder.ALPHABETICAL_DESC -> filtered.sortedWith(compareByDescending<FieldUi> { it.isPinned }.thenByDescending { it.name.lowercase() })
                else                        -> filtered.sortedWith(compareByDescending<FieldUi> { it.isPinned }.thenBy { it.name.lowercase() })
            }

            if (mostUsed != null) listOf(mostUsed) + filtered else filtered
        }.onEach { filteredFields ->
            _uiState.update { it.copy(fields = filteredFields, isLoading = false) }
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

    fun createField(name: String, color: Int? = null) {
        viewModelScope.launch {
            createFieldUseCase(name, color).onFailure { e ->
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
