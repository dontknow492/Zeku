package com.ghost.zeku.data.remote.mal.providers

import com.ghost.zeku.data.remote.mal.*
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.TrackStatus
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.provider.MediaTrackerProvider

class MalMediaTracker(
    private val api: MalApi,
    private val parser: MalResponseParser
) : MediaTrackerProvider {

    override suspend fun getUserAnimeList(
        userId: Int,
        status: TrackStatus,
        page: Int,
        perPage: Int,
        accessToken: String
    ): ApiResult<PageResult<Anime>> {
        return parser.safeApiCall(
            apiCall = {
                // MAL prefers '@me' for the logged-in user. If you have a specific MAL username,
                // you would pass it here instead of '@me'.
                api.getUserAnimeList(
                    username = "@me",
                    status = status.toMalAnimeStatus(),
                    page = page,
                    limit = perPage,
                    token = accessToken
                )
            },
            transform = { response ->
                PageResult(
                    items = response.data?.mapNotNull { it.toAnimeDomain() } ?: emptyList(),
                    currentPage = page,
                    hasNextPage = response.paging?.nextUrl != null,
                    totalPages = null
                )
            }
        )
    }

    override suspend fun getUserMangaList(
        userId: Int,
        status: TrackStatus,
        page: Int,
        perPage: Int,
        accessToken: String
    ): ApiResult<PageResult<Manga>> {
        return parser.safeApiCall(
            apiCall = {
                api.getUserMangaList(
                    username = "@me",
                    status = status.toMalMangaStatus(),
                    page = page,
                    limit = perPage,
                    token = accessToken
                )
            },
            transform = { response ->
                PageResult(
                    items = response.data?.mapNotNull { it.toMangaDomain() } ?: emptyList(),
                    currentPage = page,
                    hasNextPage = response.paging?.nextUrl != null,
                    totalPages = null
                )
            }
        )
    }

    override suspend fun updateMediaListEntry(
        accessToken: String,
        mediaId: Int,
        progress: Int?,
        status: TrackStatus?,
        score: Double?
    ): ApiResult<TrackEntry> {
        return parser.safeApiCall(
            apiCall = {
                // FIXME: Assuming Anime here because the interface doesn't specify Anime vs Manga.
                // MAL uses an integer 1-10 for scores, so we round the Double.
                api.updateAnimeListEntry(
                    mediaId = mediaId,
                    status = status?.toMalAnimeStatus(),
                    progress = progress,
                    score = score?.toInt(),
                    token = accessToken
                )
            },
            transform = { malListStatus ->
                malListStatus.toTrackEntryDomain(mediaId)
            }
        )
    }

    override suspend fun deleteMediaListEntry(
        accessToken: String,
        entryId: Int // For MAL, this will be the actual mediaId
    ): Boolean {
        return try {
            // FIXME: Assuming Anime here because the interface lacks a type discriminator.
            api.deleteAnimeListEntry(mediaId = entryId, token = accessToken)
            true
        } catch (e: Exception) {
            false
        }
    }
}