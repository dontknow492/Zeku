package com.ghost.zeku.domain.repository

import androidx.paging.PagingData
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.enum.TrackStatus
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.search.AnimeSearchFilter
import com.ghost.zeku.domain.model.search.MangaSearchFilter
import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for all Media data in the app.
 * It intelligently routes requests to the user's active provider (AniList, MAL, etc.)
 * and protects the UI from requesting unsupported data.
 */
interface MediaRepository {

    /** * Expose this so your UI/ViewModels can reactively know which provider is active.
     * Useful for showing the correct logo or settings state.
     */
    val activeProviderFlow: Flow<ProviderType>

    // ========================================================================
    // HOME SCREEN HELPERS (The Secret Sauce)
    // ========================================================================

    /**
     * Call this in your HomeViewModel!
     * It returns a list of ONLY the categories the active provider supports.
     * You can then loop through this list to generate your UI Rows.
     */
    suspend fun getAvailableAnimeCategories(): List<AnimeCategory>

    suspend fun getAvailableMangaCategories(): List<MangaCategory>

    // ========================================================================
    // DISCOVERY & SEARCH
    // ========================================================================

    fun getAnimeList(category: AnimeCategory, perPage: Int = 20): Flow<PagingData<Anime>>

    fun getMangaList(category: MangaCategory, perPage: Int = 20): Flow<PagingData<Manga>>

    fun searchAnime(
        query: String?,
        perPage: Int = 20,
        filter: AnimeSearchFilter
    ): Flow<PagingData<Anime>>

    fun searchManga(
        query: String?,
        perPage: Int = 20,
        filter: MangaSearchFilter
    ): Flow<PagingData<Manga>>


    // ========================================================================
    // USER TRACKING / LISTS
    // ========================================================================

    suspend fun getUserAnimeList(
        userId: Int,
        status: TrackStatus,
        page: Int,
        perPage: Int = 20,
        accessToken: String
    ): ApiResult<PageResult<Anime>>

    suspend fun getUserMangaList(
        userId: Int,
        status: TrackStatus,
        page: Int,
        perPage: Int = 20,
        accessToken: String
    ): ApiResult<PageResult<Manga>>

    suspend fun updateMediaListEntry(
        accessToken: String,
        mediaId: Int,
        progress: Int?,
        status: TrackStatus?,
        score: Double?
    ): ApiResult<TrackEntry>

    suspend fun deleteMediaListEntry(accessToken: String, entryId: Int): Boolean


    // ========================================================================
    // EAGER DETAILS (Offline-First Flow)
    // ========================================================================

    /**
     * Returns a stream that first emits cached data, then fetches fresh data
     * from the network, updates the cache, and emits the fresh data.
     */
    fun getAnimeDetails(id: Int): Flow<AnimeDetails>

    fun getMangaDetails(id: Int): Flow<MangaDetails>

    // NEW: Manual force refresh
    suspend fun refreshAnimeDetails(id: Int): ApiResult<Unit>
    suspend fun refreshMangaDetails(id: Int): ApiResult<Unit>

    // ========================================================================
    // LAZY DETAILS (Paginated Online-Only Flow)
    // ========================================================================

    fun getAnimeEpisodes(id: Int): Flow<PagingData<Episode>>

    fun getAnimeRecommendations(id: Int): Flow<PagingData<Anime>>

    fun getAnimeReviews(id: Int): Flow<PagingData<Review>>

    fun getMangaChapters(id: Int): Flow<PagingData<Chapter>>

    fun getMangaRecommendations(id: Int): Flow<PagingData<Manga>>

    fun getMangaReviews(id: Int): Flow<PagingData<Review>>

}