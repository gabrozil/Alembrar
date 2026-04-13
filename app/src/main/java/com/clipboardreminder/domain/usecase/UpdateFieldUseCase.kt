package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.model.Field
import com.clipboardreminder.domain.repository.FieldRepository
import com.clipboardreminder.domain.validation.ValidationResult
import com.clipboardreminder.domain.validation.validateField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateFieldUseCase @Inject constructor(private val fieldRepository: FieldRepository) {
    suspend operator fun invoke(field: Field): Result<Unit> {
        val validation = validateField(field.name)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.reason))
        }
        return runCatching { fieldRepository.updateField(field.copy(name = field.name.trim())) }
    }
}
