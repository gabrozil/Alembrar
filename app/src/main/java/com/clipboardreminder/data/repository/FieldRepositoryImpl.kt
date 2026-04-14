package com.clipboardreminder.data.repository

import com.clipboardreminder.data.local.dao.FieldDao
import com.clipboardreminder.data.local.entity.FieldEntity
import com.clipboardreminder.data.mapper.toDomain
import com.clipboardreminder.data.mapper.toEntity
import com.clipboardreminder.domain.model.Field
import com.clipboardreminder.domain.repository.FieldRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldRepositoryImpl @Inject constructor(
    private val fieldDao: FieldDao
) : FieldRepository {

    override fun getOrderedFields(): Flow<List<Field>> =
        fieldDao.getOrderedFields().map { entities -> entities.map { it.toDomain() } }

    override suspend fun createField(name: String, color: Int?): Field {
        val entity = FieldEntity(name = name, color = color)
        val id = fieldDao.insert(entity)
        return entity.copy(id = id).toDomain()
    }

    override suspend fun updateField(field: Field) {
        fieldDao.update(field.toEntity())
    }

    override suspend fun deleteField(id: Long) {
        fieldDao.deleteById(id)
    }

    override suspend fun togglePin(id: Long) {
        val entity = fieldDao.getFieldById(id).first() ?: return
        fieldDao.update(entity.copy(isPinned = !entity.isPinned))
    }
}
