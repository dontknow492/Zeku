package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.*

interface MangaDetailsProvider {
    suspend fun getMangaDetails(id: Int): ApiResult<MangaDetails>

    suspend fun getMangaChapters(id: Int, page: Int, perPage: Int = 50): ApiResult<PageResult<Chapter>>


    /**
     * LAZY: Fetches recommendations.
     * We return Manga models so you can reuse your existing Anime Card UI!
     */
    suspend fun getMangaRecommendations(id: Int, page: Int): ApiResult<PageResult<Manga>>


    /**
     * LAZY (ONLINE-ONLY): Fetches reviews or comments.
     */
    suspend fun getMangaReviews(id: Int, page: Int, perPage: Int = 20): ApiResult<PageResult<Review>>
}