package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.mapToDomainTrackStatus
import com.ghost.zeku.data.remote.anilist.model.AniListMediaListEntry
import com.ghost.zeku.data.remote.anilist.model.GraphQLRequest
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.model.media.track.TrackStatus
import com.ghost.zeku.domain.model.media.track.TrackItem
import com.ghost.zeku.domain.model.media.track.TrackUpdate
import com.ghost.zeku.domain.model.media.track.TrackerLibraryEntry
import com.ghost.zeku.domain.provider.MediaTrackerProviderV2
import com.ghost.zeku.domain.repository.AuthRepository

class AniListMediaTrackerV2(
    private val api: AniListApi,
    private val parser: AniListResponseParser,
    private val authRepository: AuthRepository
) : MediaTrackerProviderV2 {

    override val providerType: ProviderType
        get() = ProviderType.ANILIST

    override fun getSupportedStatuses(): List<TrackStatus> {
        return listOf(
            TrackStatus.CURRENT,
            TrackStatus.PLANNING,
            TrackStatus.COMPLETED,
            TrackStatus.DROPPED,
            TrackStatus.PAUSED,
            TrackStatus.REPEATING
        )
    }

    override fun getSupportedMediaTypes(): List<MediaType> {
        return listOf(MediaType.ANIME, MediaType.MANGA)
    }

    override fun getScoreFormatBounds(): Pair<Float, Float> {
        return 0f to 100f
    }

    override suspend fun isLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn(provider = providerType)
    }

    override suspend fun getTrackItem(providerMediaId: String, mediaType: MediaType): ApiResult<TrackItem> {
        return parser.safeApiCall(
            endpoint = "getTrackItem",
            apiCall = {
                // We use the Media ID to fetch the specific list entry
                val vars = GraphQLRequest.Variables(mediaId = providerMediaId.toIntOrNull())
                api.getMediaListEntry(vars)
            },
            transform = { data -> data.entry?.toTrackItem() }
        )
    }

    override suspend fun updateTrackItem(
        providerMediaId: String,
        mediaType: MediaType,
        update: TrackUpdate
    ): ApiResult<TrackItem> {
        return parser.safeApiCall(
            endpoint = "updateTrackItem",
            apiCall = {
                val vars = GraphQLRequest.Variables(
                    mediaId = providerMediaId.toIntOrNull(),
                    progress = update.progress,
                    status = update.status?.name,
                    score = update.score
                )
                api.saveMediaListEntry(vars)
            },
            transform = { data -> data.entry?.toTrackItem() }
        )
    }

    override suspend fun deleteTrackItem(providerMediaId: String, mediaType: MediaType): ApiResult<Boolean> {
        return parser.safeApiCall(
            endpoint = "deleteTrackItem",
            apiCall = {
                // Note: AniList deletion requires the Entry ID, not the Media ID.
                // Ensure the providerMediaId passed here is the Entry ID.
                val vars = GraphQLRequest.Variables(id = providerMediaId.toIntOrNull())
                api.deleteMediaListEntry(vars)
            },
            transform = { data -> data.result?.deleted ?: false }
        )
    }

    override suspend fun fetchUserLibrary(
        type: MediaType,
        status: TrackStatus?
    ): ApiResult<List<TrackItem>> {
        return parser.safeApiCall(
            endpoint = "fetchUserLibrary",
            apiCall = {
                val userId = authRepository.getUserId(providerType)
                val vars = GraphQLRequest.Variables(
                    userId = userId,
                    type = type.name,
                    status = status?.name
                )
                api.getUserLibrary(vars)
            },
            transform = { data ->
                data.collection?.lists?.flatMap { list ->
                    list.entries.map { it.toTrackItem() }
                } ?: emptyList()
            }
        )
    }

    override suspend fun fetchLibrary(
        type: MediaType,
        status: TrackStatus?
    ): ApiResult<List<TrackerLibraryEntry>> {
        return parser.safeApiCall(
            endpoint = "fetchLibrary",
            apiCall = {
                val userId = authRepository.getUserId(providerType) ?: 0
                val vars = GraphQLRequest.Variables(
                    userId = userId,
                    type = type.name,
                    status = status?.name
                )
                api.getUserLibrary(vars)
            },
            transform = { data ->
                data.collection?.lists?.flatMap { list ->
                    list.entries.map { it.toTrackerLibraryEntry(type) }
                } ?: emptyList()
            }
        )
    }

    // --- Private Mappers to keep logic clean ---

    private fun AniListMediaListEntry.toTrackItem() = TrackItem(
        id = this.id.toString(), // The Entry ID
        mediaId = this.mediaId.toString(),
        status = mapToDomainTrackStatus(status),
        progress = this.progress ?: 0,
        score = this.score?.toFloat()
    )

    private fun AniListMediaListEntry.toTrackerLibraryEntry(mediaType: MediaType) = TrackerLibraryEntry(
        providerMediaId = this.mediaId.toString(),
        title = this.media?.title?.userPreferred ?: "Unknown Title",
        coverUrl = this.media?.coverImage?.large,
        mediaType = mediaType,
        trackItem = this.toTrackItem()
    )
}