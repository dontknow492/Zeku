package com.ghost.zeku.data.repository

import androidx.paging.*
import androidx.room.useWriterConnection
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.AnimeRemoteKeys
import com.ghost.zeku.data.local.room.entities.MangaRemoteKeys
import com.ghost.zeku.data.local.room.toBaseEntity
import com.ghost.zeku.data.local.room.toDomain
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.data.paging.GenericPagingSource
import com.ghost.zeku.data.paging.category.AnimeRemoteMediator
import com.ghost.zeku.data.paging.category.MangaRemoteMediator
import com.ghost.zeku.data.paging.item.ChapterRemoteMediator
import com.ghost.zeku.data.paging.item.EpisodeRemoteMediator
import com.ghost.zeku.domain.MediaSource
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.*
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.search.AnimeSearchFilter
import com.ghost.zeku.domain.model.search.MangaSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities
import com.ghost.zeku.domain.provider.AnimeDetailsProvider
import com.ghost.zeku.domain.provider.AnimeListProvider
import com.ghost.zeku.domain.provider.MangaDetailsProvider
import com.ghost.zeku.domain.provider.MangaListProvider
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.domain.repository.UserSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Result wrapper for Repository -> UI communication.
 * Treats Errors and Loading states as Data to prevent flow crashes.
 */
sealed class DataResult<out T> {
    data class Loading(val progress: Float? = null) : DataResult<Nothing>()
    data class Success<T>(val data: T, val isFromCache: Boolean = false) : DataResult<T>()
    data class Error(val error: ApiError) : DataResult<Nothing>()
}

class MediaRepositoryImpl(
    private val settings: UserSettings,
    private val database: AppDatabase,
    private val sources: Map<ProviderType, MediaSource>
) : MediaRepository {


    // Dynamically reads the timeout from UserPreferences whenever needed!
    private val detailTtl: Long
        get() = settings.preferences.value.mediaDetailTimeout

    private val homeTtl: Long
        get() = settings.preferences.value.homeTimeout

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
        return Pair(actualType, source)
    }

    // ========================================================================
    // REUSABLE OFFLINE-FIRST ENGINE
    // ========================================================================

    /**
     * A generic, reflection-free engine that handles the Cache-Then-Network pattern.
     * It completely removes boilerplate from individual detail fetching methods.
     */
    private fun <T, E> offlineFirstFlow(
        identifier: String,
        getCached: suspend (ProviderType) -> E?,
        getUpdatedAt: (E) -> Long,
        toDomain: (E) -> T,
        fetchNetwork: suspend (MediaSource) -> ApiResult<T>,
        saveToDb: suspend (T, ProviderType) -> Unit
    ): Flow<DataResult<T>> = flow {
        emit(DataResult.Loading())
        val (sourceType, source) = getActiveProviderInfo()

        // 1. Check local DB for instant UI load
        val cachedEntity = getCached(sourceType)
        var isCacheFresh = false

        if (cachedEntity != null) {
            Napier.v { "[$identifier] Cache HIT." }
            emit(DataResult.Success(toDomain(cachedEntity), isFromCache = true))

            val cacheAgeMillis = System.currentTimeMillis() - getUpdatedAt(cachedEntity)
            isCacheFresh = cacheAgeMillis < detailTtl
        } else {
            Napier.v { "[$identifier] Cache MISS." }
        }

        // 2. Skip network if data is recent
        if (isCacheFresh) {
            Napier.v { "[$identifier] Cache is fresh. Skipping network fetch." }
            return@flow
        }

        // 3. Fetch fresh data from API
        Napier.v { "[$identifier] Fetching fresh data from network..." }
        when (val result = fetchNetwork(source)) {
            is ApiResult.Success -> {
                saveToDb(result.data, sourceType)
                emit(DataResult.Success(result.data, isFromCache = false))
            }

            is ApiResult.Error -> {
                Napier.e(result.error.cause) { "[$identifier] Network error: ${result.error.message}" }
                emit(DataResult.Error(result.error))
            }

            else -> {}
        }
    }

    // ========================================================================
    // EAGER DETAILS CACHING (Using DataResult & Offline-First Engine)
    // ========================================================================

    override fun getAnimeDetails(id: Int): Flow<DataResult<AnimeDetails>> {
        return offlineFirstFlow(
            identifier = "AnimeDetails:id=$id",
            getCached = { type -> database.animeDao().observeAnimeDetails(id, type).firstOrNull() },
            getUpdatedAt = { it.updatedAt },
            toDomain = { it.toDomain() },
            fetchNetwork = { it.getAnimeDetails(id) },
            saveToDb = { freshDetails, type ->
                database.useWriterConnection {
                    database.animeDao().upsertAnimeDetails(freshDetails.toEntity())
                    database.animeDao().upsert(freshDetails.toBaseEntity())
                }
            }
        )
    }

    override fun getMangaDetails(id: Int): Flow<DataResult<MangaDetails>> {
        return offlineFirstFlow(
            identifier = "MangaDetails:id=$id",
            getCached = { type -> database.mangaDao().observeMangaDetails(id, type).firstOrNull() },
            getUpdatedAt = { it.updatedAt },
            toDomain = { it.toDomain() },
            fetchNetwork = { it.getMangaDetails(id) },
            saveToDb = { freshDetails, type ->
                database.useWriterConnection {
                    database.mangaDao().upsertMangaDetails(freshDetails.toEntity())
                    database.mangaDao().upsert(freshDetails.toBaseEntity())
                }
            }
        )
    }

    override suspend fun refreshAnimeDetails(id: Int): ApiResult<Unit> {
        val (sourceType, provider) = getActiveProviderInfo()
        return when (val result = provider.getAnimeDetails(id)) {
            is ApiResult.Success -> {
                database.useWriterConnection {
                    database.animeDao().upsertAnimeDetails(result.data.toEntity())
                    database.animeDao().upsert(result.data.toBaseEntity())
                }
                ApiResult.Success(Unit)
            }

            is ApiResult.Error -> ApiResult.Error(result.error)
            is ApiResult.Empty -> ApiResult.Empty("Force refresh failed for id=$id")
            is ApiResult.Loading -> ApiResult.Loading()
        }
    }

    override suspend fun refreshMangaDetails(id: Int): ApiResult<Unit> {
        val (sourceType, provider) = getActiveProviderInfo()
        return when (val result = provider.getMangaDetails(id)) {
            is ApiResult.Success -> {
                database.useWriterConnection {
                    database.mangaDao().upsertMangaDetails(result.data.toEntity())
                    database.mangaDao().upsert(result.data.toBaseEntity())
                }
                ApiResult.Success(Unit)
            }

            is ApiResult.Error -> ApiResult.Error(result.error)
            is ApiResult.Empty -> ApiResult.Empty("Force refresh failed for id=$id")
            is ApiResult.Loading -> ApiResult.Loading()
        }
    }

    // ========================================================================
    // HOME SCREEN HELPERS
    // ========================================================================

    override fun getAvailableAnimeCategories(): Flow<List<AnimeCategory>> {
        return activeProviderFlow.map { currentType ->
            val actualType = if (sources.containsKey(currentType)) currentType else sources.keys.first()
            val source = sources[actualType]!!

            AnimeCategory.entries.filter { source.supportsCategory(it) }.also {
                Napier.d { "Available anime categories for $actualType: $it" }
            }
        }.distinctUntilChanged() // Prevents UI recomposition if the list hasn't actually changed
    }

    override fun getAvailableMangaCategories(): Flow<List<MangaCategory>> {
        return activeProviderFlow.map { currentType ->
            val actualType = if (sources.containsKey(currentType)) currentType else sources.keys.first()
            val source = sources[actualType]!!

            MangaCategory.entries.filter { source.supportsCategory(it) }.also {
                Napier.d { "Available manga categories for $actualType: $it" }
            }
        }.distinctUntilChanged()
    }

    // ========================================================================
    // PAGINATED LISTS (Offline-First via Room & RemoteMediator)
    // ========================================================================

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
    override fun getAnimeList(category: AnimeCategory, perPage: Int): Flow<PagingData<Anime>> {
        return activeProviderFlow.distinctUntilChanged().flatMapLatest { providerType ->
            val source = sources[providerType]
            if (source == null || !source.supportsCategory(category)) {
                return@flatMapLatest flowOf(PagingData.empty())
            }

            Pager(
                config = PagingConfig(
                    pageSize = perPage,
                    enablePlaceholders = false,
                    initialLoadSize = 20, // Override the 60-item default! Only load 1 page.
                    prefetchDistance = 5, // Fetch the next page when they are 5 items away from the end
                ),
                remoteMediator = AnimeRemoteMediator(
                    category = category,
                    currentProviderType = providerType,
                    provider = source as AnimeListProvider,
                    database = database,
                    cacheTimeoutMillis = homeTtl

                ),
                pagingSourceFactory = { database.animeDao().getAnimeByCategory(category, providerType) }
            ).flow.map { it.map { entity -> entity.toDomain() } }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
    override fun getMangaList(category: MangaCategory, perPage: Int): Flow<PagingData<Manga>> {
        return activeProviderFlow.distinctUntilChanged().flatMapLatest { providerType ->
            val source = sources[providerType]
            if (source == null || !source.supportsCategory(category)) {
                return@flatMapLatest flowOf(PagingData.empty())
            }

            Pager(
                config = PagingConfig(
                    pageSize = perPage,
                    enablePlaceholders = false,
                    initialLoadSize = 20, // Override the 60-item default! Only load 1 page.
                    prefetchDistance = 5, // Fetch the next page when they are 5 items away from the end
                ),
                remoteMediator = MangaRemoteMediator(
                    category = category,
                    currentProviderType = providerType,
                    provider = source as MangaListProvider,
                    database = database,
                    cacheTimeoutMillis = homeTtl

                ),
                pagingSourceFactory = { database.mangaDao().getMangaByCategory(category, providerType) }
            ).flow.map { it.map { entity -> entity.toDomain() } }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getHeroBanner(mediaType: MediaType, limit: Int): Flow<List<Media>> {
        return activeProviderFlow.distinctUntilChanged().flatMapLatest { activeProvider ->
            val source = sources[activeProvider] ?: return@flatMapLatest flowOf(emptyList())
            val providerType = source.getProviderType()

            channelFlow {
                // 1. Launch network request in the background
                launch {
                    try {
                        // --- TTL CHECK START ---
                        val lastUpdated = if (mediaType == MediaType.ANIME) {
                            database.remoteKeysDao().getAnimeLastUpdated(providerType, "HERO")
                        } else {
                            database.remoteKeysDao().getMangaLastUpdated(providerType, "HERO")
                        } ?: 0L // If null, it means we've never fetched it, so default to 0

                        if (System.currentTimeMillis() - lastUpdated < homeTtl) {
                            Napier.d { "Hero banner for $providerType is still fresh. Skipping network fetch." }
                            return@launch // Kills this launch block early; network is bypassed!
                        }
                        // --- TTL CHECK END ---

                        // Proceed with fetching fresh data...
                        if (mediaType == MediaType.ANIME) {
                            val response = source.getAnimeHeroList(limit)

                            if (response is ApiResult.Success && response.data.isNotEmpty()) {
                                val baseEntities = response.data.map { it.toEntity() }
                                val keys = response.data.mapIndexed { index, item ->
                                    AnimeRemoteKeys(
                                        id = item.id,
                                        source = providerType,
                                        category = "HERO",
                                        sortOrder = index,
                                        prevPage = null,
                                        nextPage = null,
                                        lastUpdated = System.currentTimeMillis() // Make sure this is captured!
                                    )
                                }

                                database.useWriterConnection {
                                    database.remoteKeysDao().clearAnimeKeys(providerType, "HERO")
                                    database.animeDao().upsertAll(baseEntities)
                                    database.remoteKeysDao().upsertAnimeKeys(keys)
                                }
                            }
                        } else {
                            val response = source.getMangaHeroList(limit)

                            if (response is ApiResult.Success && response.data.isNotEmpty()) {
                                val baseEntities = response.data.map { it.toEntity() }
                                val keys = response.data.mapIndexed { index, item ->
                                    MangaRemoteKeys(
                                        id = item.id,
                                        source = providerType,
                                        category = "HERO",
                                        sortOrder = index,
                                        prevPage = null,
                                        nextPage = null,
                                        lastUpdated = System.currentTimeMillis() // Make sure this is captured!
                                    )
                                }

                                database.useWriterConnection {
                                    database.remoteKeysDao().clearMangaKeys(providerType, "HERO")
                                    database.mangaDao().upsertAll(baseEntities)
                                    database.remoteKeysDao().upsertMangaKeys(keys)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Napier.e(e) { "Failed to fetch remote hero banner, falling back to local DB only." }
                    }
                }

                // 2. Concurrently collect from Room and send to the UI
                if (mediaType == MediaType.ANIME) {
                    database.animeDao().observeAnimeByCategory("HERO", providerType, limit).collect { entities ->
                        send(entities.map { it.toDomain() })
                    }
                } else {
                    database.mangaDao().observeMangaByCategory("HERO", providerType, limit).collect { entities ->
                        send(entities.map { it.toDomain() })
                    }
                }
            }
        }
    }

    // ========================================================================
    // DISCOVERY & SEARCH (Online-Only)
    // ========================================================================

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun searchAnime(query: String?, perPage: Int, filter: AnimeSearchFilter): Flow<PagingData<Anime>> {
        return activeProviderFlow.distinctUntilChanged()
            .combine(
                flowOf(query).debounce(searchDebounce.milliseconds).distinctUntilChanged()
            ) { type, q -> type to q }
            .flatMapLatest { (providerType, q) ->
                val source = sources[providerType] ?: return@flatMapLatest flowOf(PagingData.empty())

                Pager(
                    config = PagingConfig(
                        pageSize = perPage,
                        enablePlaceholders = false,
                        initialLoadSize = 20, // Override the 60-item default! Only load 1 page.
                        prefetchDistance = 5, // Fetch the next page when they are 5 items away from the end
                    ),
                    pagingSourceFactory = {
                        GenericPagingSource { page ->
                            source.searchAnime(
                                q,
                                page,
                                perPage,
                                filter
                            )
                        }
                    }
                ).flow
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun searchManga(query: String?, perPage: Int, filter: MangaSearchFilter): Flow<PagingData<Manga>> {
        return activeProviderFlow.distinctUntilChanged()
            .combine(
                flowOf(query).debounce(searchDebounce.milliseconds).distinctUntilChanged()
            ) { type, q -> type to q }
            .flatMapLatest { (providerType, q) ->
                val source = sources[providerType] ?: return@flatMapLatest flowOf(PagingData.empty())

                Pager(
                    config = PagingConfig(
                        pageSize = perPage,
                        enablePlaceholders = false,
                        initialLoadSize = 20, // Override the 60-item default! Only load 1 page.
                        prefetchDistance = 5, // Fetch the next page when they are 5 items away from the end
                    ),
                    pagingSourceFactory = {
                        GenericPagingSource { page ->
                            source.searchManga(
                                q,
                                page,
                                perPage,
                                filter
                            )
                        }
                    }
                ).flow
            }
    }

    override suspend fun getSearchCapabilities(provider: ProviderType, type: MediaType): SearchCapabilities {
        val source = sources[provider] ?: throw IllegalStateException("No source Implemented for $provider")
        return when (type) {
            MediaType.ANIME -> source.getAnimeSearchCapabilities()
            MediaType.MANGA -> source.getMangaSearchCapabilities()
            MediaType.UNKNOWN -> throw IllegalStateException("Invalid Media Type")
        }
    }

    // ========================================================================
    // LAZY PAGINATED FETCHES (Online Only or RemoteMediator driven)
    // ========================================================================

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getAnimeEpisodes(id: Int): Flow<PagingData<Episode>> {
        return activeProviderFlow.flatMapLatest { currentType ->
            val actualType = if (sources.containsKey(currentType)) currentType else sources.keys.first()
            val source = sources[actualType]!!

            Pager(
                config = PagingConfig(pageSize = settings.preferences.value.perPage, enablePlaceholders = false),
                remoteMediator = EpisodeRemoteMediator(
                    mediaId = id,
                    currentProviderType = actualType,
                    provider = source as AnimeDetailsProvider,
                    database = database,
                    cacheTimeoutMillis = settings.preferences.value.episodeTimeout
                ),
                pagingSourceFactory = { database.episodeDao().getEpisodesByMedia(id, actualType) }
            ).flow.map { it.map { ep -> ep.toDomain() } }
        }
    }

    override fun getAnimeRecommendations(id: Int): Flow<PagingData<Anime>> {
        val pageSize = settings.preferences.value.perPage
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = 20, // Override the 60-item default! Only load 1 page.
                prefetchDistance = 5, // Fetch the next page when they are 5 items away from the end
            )
        ) {
            GenericPagingSource { page -> getActiveProviderInfo().second.getAnimeRecommendations(id, page) }
        }.flow
    }

    override fun getAnimeReviews(id: Int): Flow<PagingData<Review>> {
        val pageSize = settings.preferences.value.perPage
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = 20, // Override the 60-item default! Only load 1 page.
                prefetchDistance = 5, // Fetch the next page when they are 5 items away from the end
            )
        ) {
            GenericPagingSource { page -> getActiveProviderInfo().second.getAnimeReviews(id, page, pageSize) }
        }.flow
    }

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getMangaChapters(id: Int): Flow<PagingData<Chapter>> {
        return activeProviderFlow.flatMapLatest { currentType ->
            val actualType = if (sources.containsKey(currentType)) currentType else sources.keys.first()
            val source = sources[actualType]!!

            Pager(
                config = PagingConfig(pageSize = settings.preferences.value.perPage, enablePlaceholders = false),
                remoteMediator = ChapterRemoteMediator(
                    mediaId = id,
                    currentProviderType = actualType,
                    provider = source as MangaDetailsProvider,
                    database = database,
                    cacheTimeoutMillis = settings.preferences.value.chapterTimeout
                ),
                pagingSourceFactory = { database.chapterDao().getChaptersByMedia(id, actualType) }
            ).flow.map { it.map { ch -> ch.toDomain() } }
        }
    }

    override fun getMangaRecommendations(id: Int): Flow<PagingData<Manga>> {
        val pageSize = settings.preferences.value.perPage
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = 20, // Override the 60-item default! Only load 1 page.
                prefetchDistance = 5, // Fetch the next page when they are 5 items away from the end
            )
        ) {
            GenericPagingSource { page -> getActiveProviderInfo().second.getMangaRecommendations(id, page) }
        }.flow
    }

    override fun getMangaReviews(id: Int): Flow<PagingData<Review>> {
        val pageSize = settings.preferences.value.perPage
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = 20, // Override the 60-item default! Only load 1 page.
                prefetchDistance = 5, // Fetch the next page when they are 5 items away from the end
            )
        ) {
            GenericPagingSource { page -> getActiveProviderInfo().second.getMangaReviews(id, page, pageSize) }
        }.flow
    }

    // ========================================================================
    // USER TRACKING / LISTS
    // ========================================================================

    override suspend fun getUserAnimeList(
        userId: Int, status: TrackStatus, page: Int, perPage: Int, accessToken: String
    ): ApiResult<PageResult<Anime>> {
        return getActiveProviderInfo().second.getUserAnimeList(userId, status, page, perPage, accessToken)
    }

    override suspend fun getUserMangaList(
        userId: Int, status: TrackStatus, page: Int, perPage: Int, accessToken: String
    ): ApiResult<PageResult<Manga>> {
        return getActiveProviderInfo().second.getUserMangaList(userId, status, page, perPage, accessToken)
    }

    override suspend fun updateMediaListEntry(
        accessToken: String, mediaId: Int, progress: Int?, status: TrackStatus?, score: Double?
    ): ApiResult<TrackEntry> {
        return getActiveProviderInfo().second.updateMediaListEntry(accessToken, mediaId, progress, status, score)
    }

    override suspend fun deleteMediaListEntry(accessToken: String, entryId: Int): Boolean {
        return getActiveProviderInfo().second.deleteMediaListEntry(accessToken, entryId)
    }
}












