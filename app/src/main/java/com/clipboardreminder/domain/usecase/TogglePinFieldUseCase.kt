package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.repository.FieldRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TogglePinFieldUseCase @Inject constructor(private val fieldRepository: FieldRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        runCatching { fieldRepository.togglePin(id) }
}
