package com.clipboardreminder.domain.model

/** Opções de ordenação compartilhadas entre campos e lembretes */
enum class SortOrder {
    ALPHABETICAL,      // A → Z
    ALPHABETICAL_DESC, // Z → A
    NEWEST,            // Mais recente primeiro
    OLDEST,            // Mais antigo primeiro
    LAST_MODIFIED,     // Última alteração
    MOST_USED          // Mais usados (lembretes)
}
