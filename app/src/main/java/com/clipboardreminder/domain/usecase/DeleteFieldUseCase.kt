package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.repository.FieldRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteFieldUseCase @Inject constructor(private val fieldRepository: FieldRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        runCatching { fieldRepository.deleteField(id) }
}
