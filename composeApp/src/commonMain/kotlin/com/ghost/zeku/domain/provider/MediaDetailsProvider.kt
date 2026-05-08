package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.MediaDetails
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.media.Review

interface MediaDetailsProvider {

    /**
     * EAGER:
     * Core media details.
     */
    suspend fun getMediaDetails(
        id: Int,
        mediaType: MediaType
    ): ApiResult<MediaDetails>

    /**
     * LAZY:
     * Recommendations.
     */
    suspend fun getRecommendations(
        mediaId: Int,
        mediaType: MediaType,
        page: Int
    ): ApiResult<PageResult<Media>>

    /**
     * LAZY:
     * Reviews/comments.
     */
    suspend fun getReviews(
        mediaId: Int,
        mediaType: MediaType,
        page: Int,
        perPage: Int = 20
    ): ApiResult<PageResult<Review>>
}