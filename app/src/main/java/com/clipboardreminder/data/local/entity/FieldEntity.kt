package com.clipboardreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fields")
data class FieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isPinned: Boolean = false,
    val color: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
