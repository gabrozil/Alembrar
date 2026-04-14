package com.clipboardreminder.domain.model

const val MOST_USED_FIELD_ID = -1L

data class FieldUi(
    val id: Long,
    val name: String,
    val isPinned: Boolean,
    val color: Int? = null,
    val isMostUsed: Boolean = false
)
