package com.ghost.zeku.presentation.screen.detail

import com.ghost.zeku.domain.model.media.*

// ---------- Data Class for State ----------
data class MediaDetailState(
    val title: String = "",
    val rating: Float = 0f,
    val year: Int = 2024,
    val episodeCount: Int = 0,
    val studio: String = "",
    val genres: List<String> = emptyList(),
    val synopsis: String = "",
    val heroImageUrl: String = "",
    val posterUrl: String = "",
    val trailer: List<MediaTrailer> = emptyList(),
    val characters: List<MediaCharacter> = emptyList(),
    val episodes: List<Episode> = emptyList(),

    val relations: List<Media> = emptyList(),
    val externalLinks: List<ExternalLink> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val recommendations: List<Media> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isWatchlisted: Boolean = false
)