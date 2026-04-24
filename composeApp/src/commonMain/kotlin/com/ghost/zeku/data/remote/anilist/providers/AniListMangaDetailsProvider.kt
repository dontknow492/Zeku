package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.*
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.provider.MangaDetailsProvider

class AniListMangaDetailsProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : MangaDetailsProvider {

    override suspend fun getMangaDetails(id: Int): ApiResult<MangaDetails> {
        return parser.safeApiCall(
            apiCall = { api.getMangaDetails(id) },
            transform = { data -> data.media?.toMangaDetailsDomain()!! }
        )
    }

    override suspend fun getMangaChapters(id: Int, page: Int, perPage: Int): ApiResult<PageResult<Chapter>> {
        // Manga reading APIs (like MangaDex) are usually used here to fill this data.
        return ApiResult.Success(PageResult(emptyList(), 1, false, 1))
    }

    override suspend fun getMangaRecommendations(id: Int, page: Int): ApiResult<PageResult<Manga>> {
        return parser.safeApiCall(
            apiCall = { api.getMediaRecommendations(id, page) },
            transform = { data ->
                val connection = data.media?.recommendations
                PageResult(
                    items = connection?.nodes?.mapNotNull { it.mediaRecommendation?.toMangaDomain() } ?: emptyList(),
                    currentPage = connection?.pageInfo?.currentPage ?: page,
                    hasNextPage = connection?.pageInfo?.hasNextPage ?: false,
                    totalPages = connection?.pageInfo?.lastPage ?: page
                )
            }
        )
    }

    override suspend fun getMangaReviews(id: Int, page: Int, perPage: Int): ApiResult<PageResult<Review>> {
        return parser.safeApiCall(
            apiCall = { api.getMediaReviews(id, page) },
            transform = { data ->
                val connection = data.media?.reviews
                PageResult(
                    items = connection?.nodes?.map { it.toDomain() } ?: emptyList(),
                    currentPage = connection?.pageInfo?.currentPage ?: page,
                    hasNextPage = connection?.pageInfo?.hasNextPage ?: false,
                    totalPages = connection?.pageInfo?.lastPage ?: page
                )
            }
        )
    }
}