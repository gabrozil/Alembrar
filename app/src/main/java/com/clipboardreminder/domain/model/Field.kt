package com.clipboardreminder.domain.model

data class Field(
    val id: Long,
    val name: String,
    val isPinned: Boolean,
    val color: Int? = null
)
