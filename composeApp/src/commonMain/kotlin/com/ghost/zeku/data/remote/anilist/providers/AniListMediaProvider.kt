package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.toMediaDomain
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ErrorType
import com.ghost.zeku.domain.model.media.MediaCategory
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.provider.MediaListProvider


class AniListMediaProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : MediaListProvider {
    override fun supportsCategory(category: MediaCategory, mediaType: MediaType): Boolean {
        return category in supportedCategories[mediaType].orEmpty()
    }

    override val supportedCategories =
        mapOf(

            MediaType.ANIME to setOf(
                MediaCategory.TRENDING,
                MediaCategory.POPULAR,
                MediaCategory.TOP_RATED,
                MediaCategory.UPCOMING,
                MediaCategory.SEASONAL,
                MediaCategory.MOVIES
            ),

            MediaType.MANGA to setOf(
                MediaCategory.TRENDING,
                MediaCategory.POPULAR,
                MediaCategory.TOP_RATED,
                MediaCategory.NEWLY_ADDED,
                MediaCategory.MANHWA,
                MediaCategory.NOVELS
            )
        )

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
                    message = "Unsupported media category: $mediaType, $category",
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
                    perPage = perPage
                )
            },
            transform = { data ->
                data.page?.let { pageData ->
                    PageResult(
                        items = pageData.media?.map { it.toMediaDomain() } ?: emptyList(),
                        currentPage = pageData.pageInfo?.currentPage ?: 1,
                        hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
                        totalPages = pageData.pageInfo?.lastPage ?: 1
                    )
                }
            }
        )
    }

    override suspend fun getHeroMediaList(
        mediaType: MediaType,
        limit: Int
    ): ApiResult<List<Media>> {
        return parser.safeApiCall(
            apiCall = {
                api.fetchHeroMediaList(mediaType = mediaType, limit = limit)
            },
            transform = { data ->
                data.page?.let { pageData ->
                    pageData.media?.map { it.toMediaDomain() } ?: emptyList()
                }
            }
        )
    }

}


