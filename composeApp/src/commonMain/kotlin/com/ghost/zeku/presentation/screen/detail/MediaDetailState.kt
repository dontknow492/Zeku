package com.ghost.zeku.presentation.screen.detail

import com.ghost.zeku.domain.model.media.MediaCharacter
import com.ghost.zeku.domain.model.media.MediaRelation

data class MediaDetailState(
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    val coverImage: String = "",
    val bannerImage: String? = null,
    val genres: List<String> = emptyList(),
    val rating: Double? = null,

    // ✅ small lists (keep)
    val characters: List<MediaCharacter> = emptyList(),
    val relations: List<MediaRelation> = emptyList(),

    val isLoading: Boolean = false,
    val error: String? = null
)