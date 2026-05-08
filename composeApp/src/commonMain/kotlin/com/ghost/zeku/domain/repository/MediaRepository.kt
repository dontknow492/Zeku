package com.ghost.zeku.domain.repository

import androidx.paging.PagingData
import com.ghost.zeku.data.repository.DataResult
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MediaCategory
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.search.MediaSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities
import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for all Media data in the app.
 * It intelligently routes requests to the user's active provider (AniList, MAL, etc.)
 * and protects the UI from requesting unsupported data.
 */
interface MediaRepository {

    /**
     * Current active provider selected by the user.
     */
    val activeProviderFlow: Flow<ProviderType>

    // ========================================================================
    // HOME / DISCOVERY
    // ========================================================================

    /**
     * Returns only categories supported by the active provider.
     *
     * Example:
     * - MAL may not support NOVELS
     * - Some scraper may only support TRENDING + POPULAR
     */
    fun getAvailableCategories(
        mediaType: MediaType
    ): Flow<List<MediaCategory>>

    /**
     * Paginated discovery feed.
     */
    fun getMediaList(
        mediaType: MediaType,
        category: MediaCategory,
        perPage: Int = 20
    ): Flow<PagingData<Media>>

    /**
     * Large featured banner carousel.
     */
    fun getHeroBanner(
        mediaType: MediaType,
        limit: Int = 10
    ): Flow<List<Media>>

    // ========================================================================
    // SEARCH
    // ========================================================================

    fun searchMedia(
        query: String?,
        perPage: Int = 20,
        filter: MediaSearchFilter
    ): Flow<PagingData<Media>>

    suspend fun getSearchCapabilities(
        provider: ProviderType,
        mediaType: MediaType
    ): SearchCapabilities

    // ========================================================================
    // DETAILS (Offline First)
    // ========================================================================

    /**
     * Emits:
     * 1. Cached data
     * 2. Fresh network data
     */
    fun getMediaDetails(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<DataResult<MediaDetails>>

    /**
     * Force refresh details.
     */
    suspend fun refreshMediaDetails(
        mediaId: Int,
        mediaType: MediaType
    ): ApiResult<Unit>

    // ========================================================================
    // PAGINATED DETAIL CONTENT
    // ========================================================================

    fun getRecommendations(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<PagingData<Media>>

    fun getReviews(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<PagingData<Review>>

    /**
     * Anime only.
     */
    fun getEpisodes(
        mediaId: Int
    ): Flow<PagingData<Episode>>

    /**
     * Manga only.
     */
    fun getChapters(
        mediaId: Int
    ): Flow<PagingData<Chapter>>
}




