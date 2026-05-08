package com.ghost.zeku.presentation.components.media.poster

import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media

/**
 * A perfectly decoupled UI model.
 * The UI doesn't know if this is Anime, Manga, AniList, or MAL.
 * It only knows what it needs to draw.
 */

/**
 * Generic UI model for any poster-style media card.
 *
 * Completely UI-focused.
 * No provider/API/domain logic should leak here.
 */
data class MediaPosterUiData(

    // ------------------------------------------------
    // Identity
    // ------------------------------------------------
    val id: Int,
    val mediaType: MediaType,

    // ------------------------------------------------
    // Main Content
    // ------------------------------------------------
    val title: String,
    val imageUrl: String,

    // Optional wide-card/banner support
    val bannerImageUrl: String? = null,

    // ------------------------------------------------
    // Metadata
    // ------------------------------------------------
    val score: Float? = null,

    /**
     * Tiny badge shown on image corner.
     *
     * Examples:
     * "TV"
     * "MOVIE"
     * "MANHWA"
     * "18+"
     * "AIRING"
     */
    val badgeText: String? = null,

    /**
     * Secondary line below title.
     *
     * Examples:
     * "24 Episodes"
     * "Studio Pierrot"
     * "Action • Fantasy"
     * "2026 • Releasing"
     */
    val subTitle: String? = null,

    /**
     * Small tertiary info.
     *
     * Examples:
     * "Episode 5"
     * "Chapter 120"
     * "Spring 2026"
     */
    val extraInfo: String? = null,

    // ------------------------------------------------
    // Progress
    // ------------------------------------------------
    val progress: Float? = null,

    /**
     * Optional progress label.
     *
     * Examples:
     * "12/24"
     * "120/200"
     */
    val progressText: String? = null,

    // ------------------------------------------------
    // State Flags
    // ------------------------------------------------
    val isNsfw: Boolean = false,
    val isNsfwRevealed: Boolean = false,

    /**
     * For currently airing/publishing content.
     */
    val isAiring: Boolean = false,

    /**
     * Allows overlays like:
     * ▶ PLAY
     * ✓ COMPLETED
     * ⟳ CONTINUE
     */
    val overlayLabel: String? = null
)


fun Media.toMediaPosterUiData(): MediaPosterUiData {
    return MediaPosterUiData(
        // Identity
        id = id,
        mediaType = mediaType,

        // Main Content
        title = title.getDisplayTitle(),
        imageUrl = coverImage,
        bannerImageUrl = bannerImage,

        // Metadata
        score = score,
        badgeText = buildBadgeText(),
        subTitle = buildSubTitle(),
        extraInfo = buildExtraInfo(),

        // Progress (user-specific, set externally when available)
        progress = null,
        progressText = null,

        // State Flags
        isNsfw = this.isAdult(),
        isNsfwRevealed = false, // User preference, set externally
        isAiring = status == MediaReleaseStatus.RELEASING,
        overlayLabel = buildOverlayLabel()
    )
}


private fun Media.buildBadgeText(): String? {
    // Priority: format > status > explicit tags
    return when {
        format != MediaFormat.UNKNOWN -> {
            format.name
                .replace("_", " ")
                .replaceFirstChar { it.uppercase() }
        }

        status == MediaReleaseStatus.RELEASING -> "AIRING"
        status == MediaReleaseStatus.NOT_YET_RELEASED -> "UPCOMING"
        else -> null
    }
}

/**
 * Builds subtitle line below the title.
 * Examples: "24 Episodes", "Studio Pierrot", "Action • Fantasy"
 */
private fun Media.buildSubTitle(): String? {
    val parts = mutableListOf<String>()

    // Episode/Chapter count
    when (mediaType) {
        MediaType.ANIME -> {
            episodes?.let { if (it > 0) parts.add("$it Episodes") }
        }

        MediaType.MANGA -> {
            chapters?.let { if (it > 0) parts.add("$it Chapters") }
        }

        else -> {}
    }

    // Studio/Author
    when (mediaType) {
        MediaType.ANIME -> studio?.let { parts.add(it) }
        MediaType.MANGA -> author?.let { parts.add(it) }
        else -> {}
    }

    // Genres (first 2-3)
    if (genres.isNotEmpty()) {
        val topGenres = genres.take(2).joinToString(" • ")
        if (topGenres.isNotBlank()) {
            // Only add genres if we don't have enough other info
            if (parts.size < 2) {
                parts.add(topGenres)
            }
        }
    }

    return parts.joinToString(" • ").ifEmpty { null }
}

/**
 * Builds tertiary info line.
 * Examples: "Spring 2026", "Episode 5", "Chapter 120"
 */
private fun Media.buildExtraInfo(): String? {
    val parts = mutableListOf<String>()

    // Season + Year
    startDate?.let { date ->
        val seasonName = when (date.month) {
            in 1..2 -> "Winter"
            in 3..5 -> "Spring"
            in 6..8 -> "Summer"
            in 9..11 -> "Fall"
            else -> null
        }
        val label = buildString {
            seasonName?.let { append(it) }
            date.year?.let {
                if (seasonName != null) append(" ")
                append(it)
            }
        }
        if (label.isNotBlank()) parts.add(label)
    }

    // Status (if not already shown as badge)
    status?.let {
        if (it != MediaReleaseStatus.UNKNOWN && it != MediaReleaseStatus.RELEASING) {
            parts.add(
                it.name
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar { char -> char.uppercase() }
            )
        }
    }

    // Popularity rank
    rank?.let { if (it > 0) parts.add("#$it") }

    return parts.joinToString(" • ").ifEmpty { null }
}

/**
 * Builds overlay label for the poster.
 * Examples: "COMPLETED", "HIATUS"
 */
private fun Media.buildOverlayLabel(): String? {
    return when (status) {
        MediaReleaseStatus.FINISHED -> "COMPLETED"
        MediaReleaseStatus.CANCELLED -> "CANCELLED"
        MediaReleaseStatus.HIATUS -> "HIATUS"
        MediaReleaseStatus.NOT_YET_RELEASED -> "UPCOMING"
        else -> null
    }
}