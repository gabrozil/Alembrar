package com.clipboardreminder.domain.validation

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

fun validateField(name: String): ValidationResult = when {
    name.isBlank() -> ValidationResult.Invalid("Nome não pode ser vazio")
    name.length > 50 -> ValidationResult.Invalid("Nome deve ter no máximo 50 caracteres")
    else -> ValidationResult.Valid
}

fun validateReminder(title: String, content: String): ValidationResult = when {
    title.isBlank() -> ValidationResult.Invalid("Título não pode ser vazio")
    title.length > 100 -> ValidationResult.Invalid("Título deve ter no máximo 100 caracteres")
    content.isBlank() -> ValidationResult.Invalid("Conteúdo não pode ser vazio")
    content.length > 5000 -> ValidationResult.Invalid("Conteúdo deve ter no máximo 5000 caracteres")
    else -> ValidationResult.Valid
}
