package com.clipboardreminder.domain.repository

import com.clipboardreminder.domain.model.Field
import kotlinx.coroutines.flow.Flow

interface FieldRepository {
    fun getOrderedFields(): Flow<List<Field>>
    suspend fun createField(name: String): Field
    suspend fun updateField(field: Field)
    suspend fun deleteField(id: Long)
    suspend fun togglePin(id: Long)
}
