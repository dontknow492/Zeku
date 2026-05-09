package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.model.media.track.TrackStatus
import com.ghost.zeku.domain.model.media.track.TrackItem
import com.ghost.zeku.domain.model.media.track.TrackUpdate
import com.ghost.zeku.domain.model.media.track.TrackerLibraryEntry

interface MediaTrackerProviderV2 {

    // --- Provider Metadata & Capabilities ---
    val providerType: ProviderType

    /**
     * Allows the UI to dynamically show/hide tracking statuses
     * based on what the specific provider supports.
     */
    fun getSupportedStatuses(): List<TrackStatus>

    /**
     * Determines if this provider handles ANIME, MANGA, or both.
     */
    fun getSupportedMediaTypes(): List<MediaType>

    /** Maximum score allowed by this provider (e.g., 10, 100, 5) */
    fun getScoreFormatBounds(): Pair<Float, Float>


    // --- Authentication State ---

    suspend fun isLoggedIn(): Boolean


    // --- Core Tracking Operations ---

    /**
     * Get the user's current tracking status for a specific media item.
     */
    suspend fun getTrackItem(providerMediaId: String, mediaType: MediaType): ApiResult<TrackItem>

    /**
     * Create or update a tracking entry.
     */
    suspend fun updateTrackItem(
        providerMediaId: String,
        mediaType: MediaType,
        update: TrackUpdate
    ): ApiResult<TrackItem>

    /**
     * Remove the item from the user's tracking list.
     */
    suspend fun deleteTrackItem(providerMediaId: String, mediaType: MediaType): ApiResult<Boolean>

    /**
     * Fetch the user's entire tracking list for syncing to the local database.
     */
    suspend fun fetchUserLibrary(
        type: MediaType,
        status: TrackStatus? = null // Null fetches the entire list
    ): ApiResult<List<TrackItem>>


    /**
     * Fetch the user's entire library (Media + Tracking info combined).
     * The implementation handles the "Bulk" fetching internally.
     */
    suspend fun fetchLibrary(
        type: MediaType,
        status: TrackStatus? = null
    ): ApiResult<List<TrackerLibraryEntry>>
}