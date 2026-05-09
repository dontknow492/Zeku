package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.*
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.MediaDetails
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.media.Review
import com.ghost.zeku.domain.provider.MediaDetailsProvider

class AniListDetailsProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : MediaDetailsProvider {
    override suspend fun getMediaDetails(
        id: Int,
        mediaType: MediaType
    ): ApiResult<MediaDetails> {
        return parser.safeApiCall(
            apiCall = { api.getMediaDetails(id, mediaType) },
            transform = { data -> data.media?.toMediaDetailsDomain() }
        )
    }

    override suspend fun getRecommendations(
        mediaId: Int,
        mediaType: MediaType,
        page: Int
    ): ApiResult<PageResult<Media>> {
        return parser.safeApiCall(
            apiCall = { api.getMediaRecommendations(mediaId, page) },
            transform = { data ->
                val connection = data.media?.recommendations
                PageResult(
                    items = connection?.nodes?.mapNotNull { it.mediaRecommendation?.toMediaDomain() } ?: emptyList(),
                    currentPage = connection?.pageInfo?.currentPage ?: page,
                    hasNextPage = connection?.pageInfo?.hasNextPage ?: false,
                    totalPages = connection?.pageInfo?.lastPage ?: page
                )
            }
        )
    }

    override suspend fun getReviews(
        mediaId: Int,
        mediaType: MediaType,
        page: Int,
        perPage: Int
    ): ApiResult<PageResult<Review>> {
        return parser.safeApiCall(
            apiCall = { api.getMediaReviews(mediaId, page) },
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
