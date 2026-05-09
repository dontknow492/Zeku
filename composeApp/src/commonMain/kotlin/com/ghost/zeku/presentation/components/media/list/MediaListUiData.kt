package com.ghost.zeku.presentation.components.media.list

import androidx.compose.runtime.Immutable
import com.ghost.zeku.domain.model.media.MediaFormat
import com.ghost.zeku.domain.model.media.MediaReleaseStatus
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.Media

@Immutable
data class MediaListUiData(
    val id: Int,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val imageUrl: String,
    // Optional wide-card/banner support
    val bannerImageUrl: String? = null,
    val mediaType: MediaType,
    val progress: Float? = null, // 0f to 1f
    val score: Float? = null, // e.g., 8.5
    val genres: List<String>? = null,
    val isWatched: Boolean = false,
    val currentEpisode: Int? = null,
    val totalEpisodes: Int? = null,
    val releaseYear: Int? = null,
    val ageRating: String? = null,
    val isAdult: Boolean = false,
    val isHidden: Boolean = false,
    val tags: List<String>? = null
) {
    fun getDisplayImageUrl(): String {
        if (imageUrl.isEmpty()) {
            return bannerImageUrl ?: ""
        }
        return imageUrl
    }
}

fun Media.toMediaListUiData(): MediaListUiData {
    return MediaListUiData(
        id = id,
        title = title.getDisplayTitle(),
        subtitle = buildSubtitle(),
        description = description,
        imageUrl = coverImage,
        bannerImageUrl = bannerImage,
        mediaType = mediaType,
        progress = null, // Progress is usually tracked separately (user-specific)
        score = score,
        genres = genres.ifEmpty { null },
        isWatched = false, // User-specific, set elsewhere
        currentEpisode = null, // User-specific, set elsewhere
        totalEpisodes = episodes,
        releaseYear = startDate?.year,
        ageRating = null, // Not directly available in Media, could be derived
        isAdult = this.isAdult(),
        isHidden = false, // User-specific, set elsewhere
        tags = tags.ifEmpty { null }
    )
}

/**
 * Builds a formatted subtitle from available metadata.
 * Examples:
 * - "TV • 12 eps • Studio Name"
 * - "Movie • 2024"
 * - "Manga • Publishing • Author Name"
 */
private fun Media.buildSubtitle(): String? {
    val parts = mutableListOf<String>()

    // Format/Type
    when {
        format != MediaFormat.UNKNOWN -> parts.add(
            format.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
    }

    // Episode/Chapter count
    when (mediaType) {
        MediaType.ANIME -> {
            if (episodes != null && episodes > 0) parts.add("$episodes eps")
        }

        MediaType.MANGA -> {
            if (chapters != null && chapters > 0) parts.add("$chapters ch")
        }

        MediaType.UNKNOWN -> null
    }

    // Release year
    startDate?.year?.let { parts.add(it.toString()) }

    // Status
    if (status != null && status != MediaReleaseStatus.UNKNOWN) {
        parts.add(status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
    }

    // Studio/Author
    when (mediaType) {
        MediaType.ANIME -> studio?.let { parts.add(it) }
        MediaType.MANGA -> author?.let { parts.add(it) }
        MediaType.UNKNOWN -> null
    }

    return parts.joinToString(" • ").ifEmpty { null }
}
