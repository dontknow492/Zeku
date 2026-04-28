package com.ghost.zeku.presentation.components.media.list

import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.calculateProgress

data class MediaListUiData(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val description: String?,
    val coverImageUrl: String,
    val subTitle: String, // e.g., "TV • 2024"
    val genres: List<String> = emptyList(), // e.g., ["Action", "Fantasy"]
    val status: String? = null, // e.g., "Releasing" or "Finished"
    val score: Float?,         // e.g., 8.5f (Out of 10 scale)
    val progress: Float?,      // 0.0f to 1.0f (for the progress bar)
    val progressText: String?, // e.g., "12 / 24 EPs" or "Ch. 105"
    val isAiring: Boolean = false,
    val isNsfw: Boolean = false,
    val isNsfwRevealed: Boolean = false
)


fun Media.toMediaListUiData(): MediaListUiData {

    val calculatedProgress = this.calculateProgress()


    // 2. Format Type and Year (e.g., "ANIME • 2024")
    val yearStr = this.startDate?.year?.toString()
    val typeStr = when (this) {
        is Anime -> "ANIME"
        is Manga -> "MANGA"
        else -> "MEDIA"
    }
    val formatAndYearStr = if (!yearStr.isNullOrBlank()) "$typeStr • $yearStr" else typeStr

    // 3. Format Progress Text (e.g., "12 / 24 EPs" vs "Ch. 105")
    val progText = if (trackEntry != null) {
        when (this) {
            is Anime -> "${trackEntry?.progress ?: 0} / ${this.episodes ?: "?"} EPs"
            is Manga -> {
                val totalStr = if (this.chapters != null) " / ${this.chapters}" else ""
                "Ch. ${trackEntry?.progress ?: 0}$totalStr"
            }

            else -> null
        }
    } else null

    // 4. Return the mapped UI Data
    return MediaListUiData(
        id = this.id,
        mediaType = this.mediaType,
        description = this.description,
        title = this.title.getPreferred(),
        coverImageUrl = this.coverImage,
        subTitle = formatAndYearStr,
        genres = this.genres,
        // Capitalizes the first letter of the status (e.g., "RELEASING" -> "Releasing")
        status = this.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() },
        score = this.score,
        progress = calculatedProgress,
        progressText = progText,
        isAiring = this.status == MediaReleaseStatus.RELEASING
    )
}
