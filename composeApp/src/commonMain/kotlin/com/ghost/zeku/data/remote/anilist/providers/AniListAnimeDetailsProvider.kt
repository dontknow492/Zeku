package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.*
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ApiResult.Success
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.provider.AnimeDetailsProvider

class AniListAnimeDetailsProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : AnimeDetailsProvider {

    override suspend fun getAnimeDetails(id: Int): ApiResult<AnimeDetails> {
        return parser.safeApiCall(
            apiCall = { api.getAnimeDetails(id) },
            transform = { data -> data.media?.toAnimeDetailsDomain()!! }
        )
    }

    override suspend fun getAnimeEpisodes(id: Int, page: Int, perPage: Int): ApiResult<PageResult<Episode>> {
        // NOTE: AniList does not natively provide a robust episode list.
        // Returning an empty result here is safe. Later, you can wrap a dedicated
        // scraping provider (like Consumet) in your Repository to fulfill this request!
        return Success(PageResult(emptyList(), 1, false, 1))
    }

    override suspend fun getAnimeRecommendations(id: Int, page: Int): ApiResult<PageResult<Anime>> {
        return parser.safeApiCall(
            apiCall = { api.getMediaRecommendations(id, page) },
            transform = { data ->
                val connection = data.media?.recommendations
                PageResult(
                    items = connection?.nodes?.mapNotNull { it.mediaRecommendation?.toAnimeDomain() } ?: emptyList(),
                    currentPage = connection?.pageInfo?.currentPage ?: page,
                    hasNextPage = connection?.pageInfo?.hasNextPage ?: false,
                    totalPages = connection?.pageInfo?.lastPage ?: page
                )
            }
        )
    }

    override suspend fun getAnimeReviews(id: Int, page: Int, perPage: Int): ApiResult<PageResult<Review>> {
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