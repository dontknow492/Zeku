package com.ghost.zeku.data.remote.mal.providers

import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.data.remote.mal.model.MalLibraryNode
import com.ghost.zeku.data.remote.mal.model.MalMediaListEntry
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.enum.TrackStatus
import com.ghost.zeku.domain.model.media.track.TrackItem
import com.ghost.zeku.domain.model.media.track.TrackUpdate
import com.ghost.zeku.domain.model.media.track.TrackerLibraryEntry
import com.ghost.zeku.domain.provider.MediaTrackerProviderV2
import com.ghost.zeku.domain.repository.AuthRepository

class MalMediaTrackerV2(
    private val api: MalApi,
    private val parser: MalResponseParser,
    private val authRepository: AuthRepository
) : MediaTrackerProviderV2 {

    override val providerType: ProviderType = ProviderType.MYANIMELIST

    override fun getSupportedStatuses(): List<TrackStatus> = listOf(
        TrackStatus.CURRENT, TrackStatus.PLANNING, TrackStatus.COMPLETED,
        TrackStatus.DROPPED, TrackStatus.PAUSED, TrackStatus.REPEATING
    )

    override fun getSupportedMediaTypes(): List<MediaType> =
        listOf(MediaType.ANIME, MediaType.MANGA)

    override fun getScoreFormatBounds(): Pair<Float, Float> = 0f to 10f

    override suspend fun isLoggedIn(): Boolean = authRepository.isUserLoggedIn(providerType)


    override suspend fun getTrackItem(providerMediaId: String, mediaType: MediaType): ApiResult<TrackItem> {
        TODO()
    }

    override suspend fun updateTrackItem(
        providerMediaId: String,
        mediaType: MediaType,
        update: TrackUpdate
    ): ApiResult<TrackItem> {
        TODO()
    }

    override suspend fun deleteTrackItem(providerMediaId: String, mediaType: MediaType): ApiResult<Boolean> {
        // Note: For MAL, we need to know if it's anime or manga to hit the right URL
        // In a real app, you'd check your DB or pass type in providerMediaId
        TODO()
    }

    override suspend fun fetchUserLibrary(
        type: MediaType,
        status: TrackStatus?
    ): ApiResult<List<TrackItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchLibrary(type: MediaType, status: TrackStatus?): ApiResult<List<TrackerLibraryEntry>> {
        TODO()
    }

    // --- Mappers ---

    private fun MalMediaListEntry.toTrackItem(mediaId: String) = TrackItem(
        id = mediaId, // MAL uses mediaId as list entry ID usually
        mediaId = mediaId,
        status = mapMalStatus(this.status),
        progress = this.episodesWatched ?: this.chaptersRead ?: 0,
        score = this.score.toFloat()
    )

    private fun MalLibraryNode.toTrackerLibraryEntry(type: MediaType) = TrackerLibraryEntry(
        providerMediaId = this.node.id.toString(),
        title = this.node.title,
        coverUrl = this.node.mainPicture?.large ?: this.node.mainPicture?.medium,
        mediaType = type,
        trackItem = this.listStatus.toTrackItem(this.node.id.toString())
    )

    private fun TrackStatus.toMalStatus(): String = when (this) {
        TrackStatus.CURRENT -> "watching"
        TrackStatus.PLANNING -> "plan_to_watch"
        TrackStatus.COMPLETED -> "completed"
        TrackStatus.DROPPED -> "dropped"
        TrackStatus.PAUSED -> "on_hold"
        TrackStatus.REPEATING -> "watching" // MAL rewatching is a boolean, not a status
        else -> "watching"
    }

    private fun mapMalStatus(status: String): TrackStatus = when (status) {
        "watching", "reading" -> TrackStatus.CURRENT
        "plan_to_watch", "plan_to_read" -> TrackStatus.PLANNING
        "completed" -> TrackStatus.COMPLETED
        "dropped" -> TrackStatus.DROPPED
        "on_hold" -> TrackStatus.PAUSED
        else -> TrackStatus.UNKNOWN
    }
}