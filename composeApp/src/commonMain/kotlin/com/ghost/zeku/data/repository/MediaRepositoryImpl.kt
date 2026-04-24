package com.ghost.zeku.data.repository

import androidx.paging.*
import androidx.room.useWriterConnection
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.toBaseEntity
import com.ghost.zeku.data.local.room.toDomain
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.data.paging.*
import com.ghost.zeku.domain.MediaSource
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.enum.TrackStatus
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.search.AnimeSearchFilter
import com.ghost.zeku.domain.model.search.MangaSearchFilter
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.domain.repository.UserSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * The single source of truth for all Media data in the app.
 * It intelligently routes requests to the user's active provider (AniList, MAL, etc.)
 * and protects the UI from requesting unsupported data.
 */
class MediaRepositoryImpl(
    private val settings: UserSettings,
    private val database: AppDatabase,
    private val sources: Map<ProviderType, MediaSource>
) : MediaRepository {

    // Dynamically reads the timeout from UserPreferences whenever needed!
    private val detailTtl: Long
        get() = settings.preferences.value.mediaDetailTimeout

    private val searchDebounce: Long
        get() = settings.preferences.value.searchDebounceMillis


    override val activeProviderFlow: Flow<ProviderType> =
        settings.preferences.map { it.activeProvider }

    /**
     * Safely resolves the current active source AND its ProviderType.
     */
    private suspend fun getActiveProviderInfo(): Pair<ProviderType, MediaSource> {
        val currentType = activeProviderFlow.first()

        val actualType = if (sources.containsKey(currentType)) currentType
        else if (sources.containsKey(ProviderType.ANILIST)) ProviderType.ANILIST
        else sources.keys.first()

        val source = sources[actualType]!!
        Napier.v { "Active provider resolved: requested=$currentType, using=$actualType" }
        return Pair(actualType, source)
    }

    // ========================================================================
    // HOME SCREEN HELPERS
    // ========================================================================

    override suspend fun getAvailableAnimeCategories(): List<AnimeCategory> {
        val (type, source) = getActiveProviderInfo()
        val categories = AnimeCategory.entries.filter { source.supportsCategory(it) }
        Napier.d { "Available anime categories for $type: $categories" }
        return categories
    }

    override suspend fun getAvailableMangaCategories(): List<MangaCategory> {
        val (type, source) = getActiveProviderInfo()
        val categories = MangaCategory.entries.filter { source.supportsCategory(it) }
        Napier.d { "Available manga categories for $type: $categories" }
        return categories
    }

    // ========================================================================
    // PAGINATED LISTS (Offline-First via Room & RemoteMediator)
    // ========================================================================

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
    override fun getAnimeList(
        category: AnimeCategory,
        perPage: Int // Driven by the ViewModel which reads it from UserPreferences!
    ): Flow<PagingData<Anime>> {

        return activeProviderFlow
            .distinctUntilChanged()
            .flatMapLatest { providerType ->

                val source = sources[providerType]
                if (source == null) {
                    Napier.w { "No source found for provider: $providerType" }
                    return@flatMapLatest flowOf(PagingData.empty())
                }

                if (!source.supportsCategory(category)) {
                    Napier.w { "Category $category not supported by provider $providerType" }
                    return@flatMapLatest flowOf(PagingData.empty())
                }

                Napier.v { "Providing anime list: category=$category, provider=$providerType, pageSize=$perPage" }
                Pager(
                    config = PagingConfig(
                        pageSize = perPage,
                        enablePlaceholders = false
                    ),
                    remoteMediator = AnimeRemoteMediator(
                        category = category,
                        currentProviderType = providerType,
                        provider = source,
                        database = database
                    ),
                    pagingSourceFactory = {
                        database.animeDao().getAnimeByCategory(category, providerType)
                    }
                ).flow
                    .map { pagingData -> pagingData.map { it.toDomain() } }
                    .onStart { Napier.v { "Anime paging flow started for $category on $providerType" } }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
    override fun getMangaList(
        category: MangaCategory,
        perPage: Int
    ): Flow<PagingData<Manga>> {

        return activeProviderFlow
            .distinctUntilChanged()
            .flatMapLatest { providerType ->

                val source = sources[providerType]
                if (source == null) {
                    Napier.w { "No source found for provider: $providerType" }
                    return@flatMapLatest flowOf(PagingData.empty())
                }

                if (!source.supportsCategory(category)) {
                    Napier.w { "Category $category not supported by provider $providerType" }
                    return@flatMapLatest flowOf(PagingData.empty())
                }

                Napier.v { "Providing manga list: category=$category, provider=$providerType, pageSize=$perPage" }
                Pager(
                    config = PagingConfig(
                        pageSize = perPage,
                        enablePlaceholders = false
                    ),
                    remoteMediator = MangaRemoteMediator(
                        category = category,
                        currentProviderType = providerType,
                        provider = source,
                        database = database
                    ),
                    pagingSourceFactory = {
                        database.mangaDao().getMangaByCategory(category, providerType)
                    }
                ).flow
                    .map { pagingData -> pagingData.map { it.toDomain() } }
                    .onStart { Napier.v { "Manga paging flow started for $category on $providerType" } }
            }
    }

    // ========================================================================
    // DISCOVERY & SEARCH (Online-Only)
    // ========================================================================

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun searchAnime(
        query: String?,
        perPage: Int,
        filter: AnimeSearchFilter
    ): Flow<PagingData<Anime>> {

        return activeProviderFlow
            .distinctUntilChanged()
            .combine(
                flowOf(query)
                    .debounce(searchDebounce.milliseconds)
                    .distinctUntilChanged()
            ) { providerType, q ->
                providerType to q
            }
            .flatMapLatest { (providerType, q) ->

                val source = sources[providerType]
                if (source == null) {
                    Napier.w { "No source for provider $providerType during anime search" }
                    return@flatMapLatest flowOf(PagingData.empty())
                }

                Napier.d { "Anime search: query='$q', provider=$providerType, filter=$filter" }
                Pager(
                    config = PagingConfig(
                        pageSize = perPage,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        GenericPagingSource { page ->
                            source.searchAnime(query = q, page = page, perPage = perPage, filter = filter)
                        }
                    }
                ).flow
                    .onStart { Napier.v { "Anime search paging started for query='$q'" } }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun searchManga(
        query: String?,
        perPage: Int,
        filter: MangaSearchFilter
    ): Flow<PagingData<Manga>> {

        return activeProviderFlow
            .distinctUntilChanged()
            .combine(
                flowOf(query)
                    .debounce(searchDebounce.milliseconds)
                    .distinctUntilChanged()
            ) { providerType, q ->
                providerType to q
            }
            .flatMapLatest { (providerType, q) ->

                val source = sources[providerType]
                if (source == null) {
                    Napier.w { "No source for provider $providerType during manga search" }
                    return@flatMapLatest flowOf(PagingData.empty())
                }

                Napier.d { "Manga search: query='$q', provider=$providerType, filter=$filter" }
                Pager(
                    config = PagingConfig(
                        pageSize = perPage,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        GenericPagingSource { page ->
                            source.searchManga(query = q, page = page, perPage = perPage, filter = filter)
                        }
                    }
                ).flow
                    .onStart { Napier.v { "Manga search paging started for query='$q'" } }
            }
    }

    // ========================================================================
    // USER TRACKING / LISTS
    // ========================================================================

    override suspend fun getUserAnimeList(
        userId: Int,
        status: TrackStatus,
        page: Int,
        perPage: Int,
        accessToken: String
    ): ApiResult<PageResult<Anime>> {
        Napier.d { "Fetching user anime list: userId=$userId, status=$status, page=$page, perPage=$perPage" }
        val result = getActiveProviderInfo().second.getUserAnimeList(userId, status, page, perPage, accessToken)
        when (result) {
            is ApiResult.Success -> Napier.i { "User anime list fetched successfully (page $page, ${result.data.items.size} items)" }
            is ApiResult.Error -> Napier.e(result.error.cause) { "Failed to fetch user anime list: ${result.error.message}" }
            else -> {}
        }
        return result
    }

    override suspend fun getUserMangaList(
        userId: Int,
        status: TrackStatus,
        page: Int,
        perPage: Int,
        accessToken: String
    ): ApiResult<PageResult<Manga>> {
        Napier.d { "Fetching user manga list: userId=$userId, status=$status, page=$page, perPage=$perPage" }
        val result = getActiveProviderInfo().second.getUserMangaList(userId, status, page, perPage, accessToken)
        when (result) {
            is ApiResult.Success -> Napier.i { "User manga list fetched successfully (page $page, ${result.data.items.size} items)" }
            is ApiResult.Error -> Napier.e(result.error.cause) { "Failed to fetch user manga list: ${result.error.message}" }
            else -> {}
        }
        return result
    }

    override suspend fun updateMediaListEntry(
        accessToken: String,
        mediaId: Int,
        progress: Int?,
        status: TrackStatus?,
        score: Double?
    ): ApiResult<TrackEntry> {
        val result = getActiveProviderInfo().second.updateMediaListEntry(accessToken, mediaId, progress, status, score)
        when (result) {
            is ApiResult.Success -> Napier.i { "Media list entry updated: mediaId=$mediaId -> ${result.data}" }
            is ApiResult.Error -> Napier.e(result.error.cause) { "Update failed for mediaId=$mediaId: ${result.error.message}" }
            else -> {}
        }
        return result
    }

    override suspend fun deleteMediaListEntry(accessToken: String, entryId: Int): Boolean {
        val success = getActiveProviderInfo().second.deleteMediaListEntry(accessToken, entryId)
        if (success) Napier.i { "Media list entry deleted: entryId=$entryId" }
        else Napier.w { "Failed to delete media list entry: entryId=$entryId" }
        return success
    }

    // ========================================================================
    // EAGER DETAILS CACHING
    // ========================================================================

    override fun getAnimeDetails(id: Int): Flow<AnimeDetails> = flow {
        val (source, provider) = getActiveProviderInfo()
        Napier.v { "getAnimeDetails: id=$id, provider=$source" }

        // 1. INSTANT OFFLINE LOAD: Read the JSON cache from Room and emit immediately
        val cached = database.animeDao().observeAnimeDetails(id, source).firstOrNull()
        if (cached != null) {
            Napier.v { "Anime details cache HIT for id=$id" }
            emit(cached.toDomain())

            val cacheAgeMillis = System.currentTimeMillis() - cached.updatedAt

            // Replaced hardcoded value with dynamic preference check
            if (cacheAgeMillis < detailTtl) {
                Napier.v { "Anime details cache is still fresh. Skipping network fetch for id=$id" }
                return@flow
            }
        } else {
            Napier.v { "Anime details cache MISS for id=$id" }
        }

        // 2. BACKGROUND NETWORK FETCH
        when (val result = provider.getAnimeDetails(id)) {
            is ApiResult.Success -> {
                val freshDetails = result.data
                database.useWriterConnection {
                    database.animeDao().upsertAnimeDetails(freshDetails.toEntity(source))
                    database.animeDao().upsert(freshDetails.toBaseEntity(source))
                }
                Napier.v { "Saved anime details to cache: id=$id" }
                emit(freshDetails)
            }

            is ApiResult.Error -> {
                Napier.e(result.error.cause) { "Network error fetching anime details id=$id: ${result.error.message}" }
                if (cached == null) throw Exception(result.error.message)
            }

            else -> {}
        }
    }

    override fun getMangaDetails(id: Int): Flow<MangaDetails> = flow {
        val (source, provider) = getActiveProviderInfo()
        Napier.v { "getMangaDetails: id=$id, provider=$source" }

        val cached = database.mangaDao().observeMangaDetails(id, source).firstOrNull()
        if (cached != null) {
            Napier.v { "Manga details cache HIT for id=$id" }
            emit(cached.toDomain())

            val cacheAgeMillis = System.currentTimeMillis() - cached.updatedAt

            // Replaced hardcoded value with dynamic preference check
            if (cacheAgeMillis < detailTtl) {
                Napier.v { "Manga details cache is still fresh. Skipping network fetch for id=$id" }
                return@flow
            }
        } else {
            Napier.v { "Manga details cache MISS for id=$id" }
        }

        when (val result = provider.getMangaDetails(id)) {
            is ApiResult.Success -> {
                val freshDetails = result.data
                database.useWriterConnection {
                    database.mangaDao().upsertMangaDetails(freshDetails.toEntity(source))
                    database.mangaDao().upsert(freshDetails.toBaseEntity(source))
                }
                Napier.v { "Saved manga details to cache: id=$id" }
                emit(freshDetails)
            }

            is ApiResult.Error -> {
                Napier.e(result.error.cause) { "Network error fetching manga details id=$id: ${result.error.message}" }
                if (cached == null) throw Exception(result.error.message)
            }

            else -> {}
        }
    }

    override suspend fun refreshAnimeDetails(id: Int): ApiResult<Unit> {
        val (source, provider) = getActiveProviderInfo()
        return when (val result = provider.getAnimeDetails(id)) {
            is ApiResult.Success -> {
                val freshDetails = result.data
                database.useWriterConnection {
                    database.animeDao().upsertAnimeDetails(freshDetails.toEntity(source))
                    database.animeDao().upsert(freshDetails.toBaseEntity(source))
                }
                ApiResult.Success(Unit)
            }

            is ApiResult.Error -> ApiResult.Error(result.error)
            is ApiResult.Empty -> ApiResult.Empty("Force refresh failed for id=$id")
            is ApiResult.Loading -> ApiResult.Loading()
        }
    }

    override suspend fun refreshMangaDetails(id: Int): ApiResult<Unit> {
        val (source, provider) = getActiveProviderInfo()
        return when (val result = provider.getMangaDetails(id)) {
            is ApiResult.Success -> {
                val freshDetails = result.data
                database.useWriterConnection {
                    database.mangaDao().upsertMangaDetails(freshDetails.toEntity(source))
                    database.mangaDao().upsert(freshDetails.toBaseEntity(source))
                }
                ApiResult.Success(Unit)
            }

            is ApiResult.Error -> ApiResult.Error(result.error)
            is ApiResult.Empty -> ApiResult.Empty("Force refresh failed for id=$id")
            is ApiResult.Loading -> ApiResult.Loading()
        }
    }

    // ========================================================================
    // LAZY PAGINATED FETCHES (Online Only)
    // ========================================================================

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getAnimeEpisodes(id: Int): Flow<PagingData<Episode>> {
        Napier.v { "Lazy fetch anime episodes for id=$id" }

        return activeProviderFlow.flatMapLatest { currentType ->
            val actualType = if (sources.containsKey(currentType)) currentType else sources.keys.first()
            val source = sources[actualType]!!

            Pager(
                // Use the user's preferred page size directly from Settings!
                config = PagingConfig(pageSize = settings.preferences.value.perPage, enablePlaceholders = false),
                remoteMediator = EpisodeRemoteMediator(
                    mediaId = id,
                    currentProviderType = actualType,
                    provider = source,
                    database = database,
                    // NOTE: Make sure to update your EpisodeRemoteMediator constructor to accept `cacheTimeoutMillis: Long`
                    cacheTimeoutMillis = settings.preferences.value.episodeTimeout
                ),
                pagingSourceFactory = {
                    database.episodeDao().getEpisodesByMedia(id, actualType)
                }
            ).flow.map { pagingData ->
                pagingData.map { it.toDomain() }
            }
        }.onStart { Napier.v { "Anime episodes paging started for id=$id" } }
    }

    override fun getAnimeRecommendations(id: Int): Flow<PagingData<Anime>> {
        val pageSize = settings.preferences.value.perPage
        return Pager(config = PagingConfig(pageSize = pageSize)) {
            GenericPagingSource { page ->
                getActiveProviderInfo().second.getAnimeRecommendations(id, page)
            }
        }.flow
    }

    override fun getAnimeReviews(id: Int): Flow<PagingData<Review>> {
        val pageSize = settings.preferences.value.perPage
        return Pager(config = PagingConfig(pageSize = pageSize)) {
            GenericPagingSource { page ->
                getActiveProviderInfo().second.getAnimeReviews(id, page, pageSize)
            }
        }.flow
    }

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getMangaChapters(id: Int): Flow<PagingData<Chapter>> {
        Napier.v { "Lazy fetch manga chapters for id=$id" }

        return activeProviderFlow.flatMapLatest { currentType ->
            val actualType = if (sources.containsKey(currentType)) currentType else sources.keys.first()
            val source = sources[actualType]!!

            Pager(
                config = PagingConfig(pageSize = settings.preferences.value.perPage, enablePlaceholders = false),
                remoteMediator = ChapterRemoteMediator(
                    mediaId = id,
                    currentProviderType = actualType,
                    provider = source,
                    database = database,
                    // NOTE: Make sure to update your ChapterRemoteMediator constructor to accept `cacheTimeoutMillis: Long`
                    cacheTimeoutMillis = settings.preferences.value.chapterTimeout
                ),
                pagingSourceFactory = {
                    database.chapterDao().getChaptersByMedia(id, actualType)
                }
            ).flow.map { pagingData ->
                pagingData.map { it.toDomain() }
            }
        }.onStart { Napier.v { "Manga chapters paging started for id=$id" } }
    }

    override fun getMangaRecommendations(id: Int): Flow<PagingData<Manga>> {
        val pageSize = settings.preferences.value.perPage
        return Pager(config = PagingConfig(pageSize = pageSize)) {
            GenericPagingSource { page ->
                getActiveProviderInfo().second.getMangaRecommendations(id, page)
            }
        }.flow
    }

    override fun getMangaReviews(id: Int): Flow<PagingData<Review>> {
        val pageSize = settings.preferences.value.perPage
        return Pager(config = PagingConfig(pageSize = pageSize)) {
            GenericPagingSource { page ->
                getActiveProviderInfo().second.getMangaReviews(id, page, pageSize)
            }
        }.flow
    }
}