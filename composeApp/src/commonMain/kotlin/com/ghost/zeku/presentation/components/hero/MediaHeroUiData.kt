package com.ghost.zeku.presentation.components.hero

import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media

data class MediaHeroUiData(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val bannerImageUrl: String, // High-res wide image
    val coverImageUrl: String,  // Fallback if banner is missing
    val description: String,
    val genres: List<String>,
    val badgeText: String?
)

// ============================================================================
// UI STATE & MAPPER
// ============================================================================

fun Media.toHeroUiData(): MediaHeroUiData {
    return MediaHeroUiData(
        id = this.id,
        mediaType = this.mediaType,
        title = this.title.getPreferred(),
        bannerImageUrl = this.bannerImage ?: this.coverImage,
        coverImageUrl = this.coverImage,
        description = this.description ?: "",
        genres = this.genres.take(3), // Max 3 for clean UI
        badgeText = this.status?.name?.replaceFirstChar { it.uppercase() }
    )
}



