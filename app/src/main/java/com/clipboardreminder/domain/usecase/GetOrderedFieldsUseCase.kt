package com.clipboardreminder.domain.usecase

import com.clipboardreminder.domain.model.FieldUi
import com.clipboardreminder.domain.model.MOST_USED_FIELD_ID
import com.clipboardreminder.domain.repository.FieldRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetOrderedFieldsUseCase @Inject constructor(
    private val fieldRepository: FieldRepository
) {
    operator fun invoke(): Flow<List<FieldUi>> =
        fieldRepository.getOrderedFields().map { fields ->
            val mostUsed = FieldUi(
                id = MOST_USED_FIELD_ID,
                name = "Mais Usados",
                isPinned = false,
                isMostUsed = true
            )
            val fieldUis = fields.map { field ->
                FieldUi(
                    id = field.id,
                    name = field.name,
                    isPinned = field.isPinned,
                    isMostUsed = false
                )
            }
            listOf(mostUsed) + fieldUis
        }
}
