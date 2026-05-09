package com.ghost.zeku.domain.model.media.track

import com.ghost.zeku.domain.model.media.MediaType


// Represents the show/manga itself on the provider's platform
data class TrackerMediaItem(
    val providerMediaId: String,
    val title: String,
    val type: MediaType, // Using your enum
    val coverUrl: String? = null
)

data class TrackerLibraryEntry(
    val providerMediaId: String,
    val title: String,
    val coverUrl: String?,
    val mediaType: MediaType, // Your enum (ANIME or MANGA)
    val trackItem: TrackItem  // The progress, status, score, etc.
)

// Represents the user's actual progress/tracking entry
data class TrackItem(
    val id: String,
    val mediaId: String,
    val status: TrackStatus,
    val progress: Int,           // Episodes watched or chapters read
    val totalVolumes: Int? = null,
    val score: Float?,
    val startedAt: Long? = null,
    val finishedAt: Long? = null
)

// The payload sent from the UI when a user changes their progress
data class TrackUpdate(
    val status: TrackStatus? = null,
    val progress: Int? = null,
    val score: Float? = null,
    val startedAt: Long? = null,
    val finishedAt: Long? = null
)