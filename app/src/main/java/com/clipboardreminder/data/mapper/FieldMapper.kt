package com.clipboardreminder.data.mapper

import com.clipboardreminder.data.local.entity.FieldEntity
import com.clipboardreminder.domain.model.Field

fun FieldEntity.toDomain() = Field(id = id, name = name, isPinned = isPinned)

fun Field.toEntity() = FieldEntity(id = id, name = name, isPinned = isPinned)
