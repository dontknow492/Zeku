package com.ghost.zeku.data.remote.mal.providers

import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.data.remote.mal.toMediaDomain
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ErrorType
import com.ghost.zeku.domain.model.enum.MediaCategory
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.provider.MediaListProvider

class MalMediaProvider(
    private val api: MalApi,
    private val parser: MalResponseParser
) : MediaListProvider {

    override val supportedCategories: Map<MediaType, Set<MediaCategory>> =
        mapOf(
            // =========================================================
            // ANIME
            // =========================================================

            MediaType.ANIME to setOf(
                MediaCategory.TRENDING,
                MediaCategory.POPULAR,
                MediaCategory.TOP_RATED,
                MediaCategory.UPCOMING,
                MediaCategory.SEASONAL,
                MediaCategory.MOVIES
            ),

            // =========================================================
            // MANGA
            // =========================================================

            MediaType.MANGA to setOf(
                MediaCategory.TRENDING,
                MediaCategory.POPULAR,
                MediaCategory.TOP_RATED,
                MediaCategory.NEWLY_ADDED,
                MediaCategory.MANHWA,
                MediaCategory.NOVELS
            )
        )

    override fun supportsCategory(
        category: MediaCategory,
        mediaType: MediaType
    ): Boolean {
        return supportedCategories[mediaType]
            ?.contains(category)
            ?: false
    }

    override suspend fun getMediaList(
        category: MediaCategory,
        mediaType: MediaType,
        page: Int,
        perPage: Int
    ): ApiResult<PageResult<Media>> {
        if (!supportsCategory(category, mediaType)) {
            return ApiResult.Error(
                error = ApiError(
                    type = ErrorType.UNSUPPORTED_FEATURE,
                    code = 400,
                    message = "Invalid category",
                    recoverable = false
                )
            )
        }
        return parser.safeApiCall(
            apiCall = {
                api.fetchMediaList(
                    mediaType = mediaType,
                    category = category,
                    page = page,
                    limit = perPage
                )
            },
            transform = { response ->
                PageResult(
                    items = response.data?.mapNotNull { it.toMediaDomain(mediaType = mediaType) } ?: emptyList(),
                    currentPage = page,
                    hasNextPage = response.paging?.nextUrl != null,
                    totalPages = null // MAL doesn't provide total pages in the response
                )
            }

        )
    }

    override suspend fun getHeroMediaList(
        mediaType: MediaType,
        limit: Int
    ): ApiResult<List<Media>> {

        return parser.safeApiCall(
            apiCall = {
                api.fetchHeroList(mediaType = mediaType, limit = limit)
            },
            transform = { response ->
                response.data?.mapNotNull { it.toMediaDomain(mediaType = mediaType) }
            }
        )
    }
}