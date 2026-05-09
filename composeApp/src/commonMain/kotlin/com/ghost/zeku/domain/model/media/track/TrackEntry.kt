package com.ghost.zeku.domain.model.media.track

import kotlinx.serialization.Serializable

// Represents a user's progress on a specific Anime or Manga
@Serializable
data class TrackEntry(
    val entryId: Int,    // The unique ID of this item on the user's list
    val mediaId: Int,    // The ID of the actual Anime/Manga
    val status: TrackStatus,
    val progress: Int,   // Episode or Chapter number
    val score: Double?,
    val totalProgress: Int? = null // Added to calculate progress bar (e.g. 12 episodes total)
)