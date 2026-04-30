package com.ghost.zeku.presentation.components.media.poster

import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.calculateProgress

/**
 * A perfectly decoupled UI model.
 * The UI doesn't know if this is Anime, Manga, AniList, or MAL.
 * It only knows what it needs to draw.
 */
data class MediaPosterUiData(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val imageUrl: String,
    val score: Float? = null,
    val badgeText: String? = null,
    val subTitle: String? = null,
    val progress: Float? = null,
    val isNsfw: Boolean = false, // 👈 new
    val isNsfwRevealed: Boolean = false // 👈 new
)

// ============================================================================
// MAPPERS (Domain -> UI)
// ============================================================================

fun Media.toPosterUiData(): MediaPosterUiData {
    // Calculate progress if it exists
    val calculatedProgress = this.calculateProgress()


    return MediaPosterUiData(
        id = this.id,
        // Uses the helper we created in MediaTitle.kt!
        title = this.title.getDisplayTitle(),
        imageUrl = this.coverImage,
        score = this.score,
        badgeText = generateBadgeText(this),
        subTitle = this.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() },
        progress = calculatedProgress,
        mediaType = this.mediaType,
    )
}

/**
 * Helper to generate a contextual badge based on the media type.
 */
private fun generateBadgeText(media: Media): String? {
    return when (media) {
        is Anime -> {
            if (media.episodes != null) "${media.episodes} Eps"
            else media.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
        }

        is Manga -> {

            if (media.chapters != null) "${media.chapters} Ch"
            else media.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
        }

        else -> null
    }
}