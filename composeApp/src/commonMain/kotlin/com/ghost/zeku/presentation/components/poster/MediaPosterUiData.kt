package com.ghost.zeku.presentation.components.poster

import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.Media

/**
 * A perfectly decoupled UI model.
 * The UI doesn't know if this is Anime, Manga, AniList, or MAL.
 * It only knows what it needs to draw.
 */
data class MediaPosterUiData(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val score: Float? = null,
    val badgeText: String? = null, // e.g., "12 Eps", "Finished", or "Ch. 45"
    val subTitle: String? = null, // e.g., "Finished", "Unknown, etc
    val progress: Float? = null // 0.0f to 1.0f for a progress bar
)

// ============================================================================
// MAPPERS (Domain -> UI)
// ============================================================================

fun Media.toPosterUiData(): MediaPosterUiData {
    // Calculate progress if it exists
    val calculatedProgress = if (trackEntry != null && trackEntry?.totalProgress != null) {
        val current = trackEntry?.progress?.toFloat() ?: 0f
        val total = trackEntry?.totalProgress?.toFloat() ?: 1f
        if (total > 0f) (current / total).coerceIn(0f, 1f) else null
    } else null

    return MediaPosterUiData(
        id = this.id,
        // Uses the helper we created in MediaTitle.kt!
        title = this.title.getPreferred(),
        imageUrl = this.coverImage,
        score = this.score,
        badgeText = generateBadgeText(this),
        subTitle = this.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() },
        progress = calculatedProgress
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