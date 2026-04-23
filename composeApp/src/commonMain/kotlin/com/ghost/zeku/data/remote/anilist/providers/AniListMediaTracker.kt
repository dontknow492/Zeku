package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.*
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.TrackStatus
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.provider.MediaTrackerProvider

// Assume you have standard imports for ApiResult, PageResult, etc.

class AniListMediaTracker(
    private val aniListApi: AniListApi,
    private val parser: AniListResponseParser
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
                aniListApi.getUserAnimeList(
                    userId,
                    mapDomainToTrackStatus(status),
                    page,
                    perPage,
                    accessToken
                )
            },
            transform = { data ->
                data.page?.let { pageData ->
                    PageResult(
                        // mapNotNull ignores any items where 'media' was null
                        items = pageData.mediaList?.mapNotNull { it.toAnimeDomain() } ?: emptyList(),
                        currentPage = pageData.pageInfo?.currentPage ?: 1,
                        hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
                        totalPages = pageData.pageInfo?.lastPage ?: 1
                    )
                }
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
                aniListApi.getUserMangaList(
                    userId,
                    mapDomainToTrackStatus(status),
                    page,
                    perPage,
                    accessToken
                )
            },
            transform = { data ->
                data.page?.let { pageData ->
                    PageResult(
                        items = pageData.mediaList?.mapNotNull { it.toMangaDomain() } ?: emptyList(),
                        currentPage = pageData.pageInfo?.currentPage ?: 1,
                        hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
                        totalPages = pageData.pageInfo?.lastPage ?: 1
                    )
                }
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
                aniListApi.updateMediaListEntry(
                    token = accessToken, // Format auth token if not done by OkHttp Interceptor
                    mediaId = mediaId,
                    progress = progress,
                    status = status?.let { mapDomainToTrackStatus(it) },
                    score = score?.toFloat()
                )
            },
            transform = { response ->
                // Make sure `toTrackEntry` is public in your Mapper file so it can be called here!
                // NOTE: Adjust "SaveMediaListEntry" to whatever your mutation response variable is named
                response.entry?.toTrackEntry(
                    mediaId = mediaId,
                    totalProgress = null // We don't have total episodes from a simple mutation response
                ) ?: throw IllegalStateException("Failed to update entry: Response was null")
            }
        )
    }

    override suspend fun deleteMediaListEntry(accessToken: String, entryId: Int): Boolean {
        // Because the interface returns Boolean instead of ApiResult,
        // we use a standard try/catch block here.
        return try {
            val response = aniListApi.deleteMediaListEntry(
                token = accessToken,
                entryId = entryId
            )


            // Adjust "DeleteMediaListEntry" and "deleted" to match your mutation response variables
            response.data?.result?.deleted ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}


