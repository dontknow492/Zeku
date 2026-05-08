package com.ghost.zeku.data.repository

import androidx.paging.*
import androidx.room.useWriterConnection
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.MediaRemoteKeys
import com.ghost.zeku.data.local.room.toBaseEntity
import com.ghost.zeku.data.local.room.toDomain
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.data.paging.GenericPagingSource
import com.ghost.zeku.data.paging.category.MediaRemoteMediator
import com.ghost.zeku.data.paging.item.ChapterRemoteMediator
import com.ghost.zeku.data.paging.item.EpisodeRemoteMediator
import com.ghost.zeku.domain.MediaSource
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MediaCategory
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.search.MediaSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.domain.repository.UserSettings
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

    data class Loading(
        val progress: Float? = null
    ) : DataResult<Nothing>()

    data class Success<T>(
        val data: T,
        val isFromCache: Boolean = false
    ) : DataResult<T>()

    data class Error(
        val error: ApiError
    ) : DataResult<Nothing>()
}

class MediaRepositoryImpl(
    private val settings: UserSettings,
    private val database: AppDatabase,
    private val sources: Map<ProviderType, MediaSource>
) : MediaRepository {

    // =========================================================
    // SETTINGS
    // =========================================================

    private val detailTtl: Long
        get() = settings.preferences.value.mediaDetailTimeout

    private val homeTtl: Long
        get() = settings.preferences.value.homeTimeout

    private val searchDebounce: Long
        get() = settings.preferences.value.searchDebounceMillis

    private val pageSize: Int
        get() = settings.preferences.value.perPage

    override val activeProviderFlow: Flow<ProviderType> =
        settings.preferences
            .map { it.activeProvider }
            .distinctUntilChanged()

    // =========================================================
    // PROVIDER HELPERS
    // =========================================================

    private fun resolveProvider(type: ProviderType): MediaSource {
        return sources[type]
            ?: sources[ProviderType.ANILIST]
            ?: sources.values.first()
    }

    private suspend fun getActiveSource(): MediaSource {
        return resolveProvider(activeProviderFlow.first())
    }

    private suspend fun getActiveProviderInfo(): Pair<ProviderType, MediaSource> {
        val requested = activeProviderFlow.first()
        val source = resolveProvider(requested)
        return source.providerType to source
    }

    // =========================================================
    // OFFLINE FIRST ENGINE
    // =========================================================

    private fun <T, E> offlineFirstFlow(
        identifier: String,
        getCached: suspend (ProviderType) -> E?,
        getUpdatedAt: (E) -> Long,
        toDomain: (E) -> T,
        fetchNetwork: suspend (MediaSource) -> ApiResult<T>,
        saveToDb: suspend (T, ProviderType) -> Unit
    ): Flow<DataResult<T>> = flow {

        emit(DataResult.Loading())

        val (providerType, source) = getActiveProviderInfo()

        val cached = getCached(providerType)

        var cacheFresh = false

        if (cached != null) {

            emit(
                DataResult.Success(
                    data = toDomain(cached),
                    isFromCache = true
                )
            )

            val age = System.currentTimeMillis() - getUpdatedAt(cached)

            cacheFresh = age < detailTtl
        }

        if (cacheFresh) return@flow

        when (val result = fetchNetwork(source)) {

            is ApiResult.Success -> {

                saveToDb(result.data, providerType)

                emit(
                    DataResult.Success(
                        data = result.data,
                        isFromCache = false
                    )
                )
            }

            is ApiResult.Error -> {
                emit(DataResult.Error(result.error))
            }

            else -> Unit
        }
    }

    // =========================================================
    // CATEGORY HELPERS
    // =========================================================

    override fun getAvailableCategories(
        mediaType: MediaType
    ): Flow<List<MediaCategory>> {
        return activeProviderFlow.map { type ->

            val source = resolveProvider(type)

            MediaCategory.entries.filter {
                source.supportsCategory(it, mediaType)
            }
        }
    }

    // =========================================================
    // LISTS
    // =========================================================

    @OptIn(
        ExperimentalCoroutinesApi::class,
        ExperimentalPagingApi::class
    )
    override fun getMediaList(mediaType: MediaType, category: MediaCategory, perPage: Int): Flow<PagingData<Media>> {

        return activeProviderFlow.flatMapLatest { providerType ->

            val source = resolveProvider(providerType)

            if (!source.supportsCategory(category, mediaType)) {
                return@flatMapLatest flowOf(PagingData.empty())
            }

            Pager(
                config = PagingConfig(
                    pageSize = perPage,
                    enablePlaceholders = false,
                    initialLoadSize = perPage,
                    prefetchDistance = 5
                ),

                remoteMediator = MediaRemoteMediator(
                    category = category,
                    mediaType = mediaType,
                    providerType = providerType,
                    provider = source,
                    database = database,
                    cacheTimeoutMillis = homeTtl
                ),

                pagingSourceFactory = {
                    database.mediaDao()
                        .getMediaByCategory(
                            category = category.name,
                            mediaType = mediaType,
                            provider = providerType
                        )
                }
            ).flow.map { paging ->
                paging.map { entity ->
                    entity.toDomain()
                }
            }
        }
    }

    // =========================================================
    // SEARCH
    // =========================================================

    @OptIn(
        ExperimentalCoroutinesApi::class,
        FlowPreview::class
    )
    override fun searchMedia(query: String?, perPage: Int, filter: MediaSearchFilter): Flow<PagingData<Media>> {

        return activeProviderFlow
            .combine(
                flowOf(query)
                    .debounce(searchDebounce.milliseconds)
                    .distinctUntilChanged()
            ) { provider, q ->
                provider to q
            }
            .flatMapLatest { (providerType, q) ->

                val source = resolveProvider(providerType)

                Pager(
                    config = PagingConfig(
                        pageSize = perPage,
                        enablePlaceholders = false,
                        initialLoadSize = perPage,
                        prefetchDistance = 5
                    )
                ) {
                    GenericPagingSource { page ->

                        source.searchMedia(
                            query = q,
                            page = page,
                            perPage = perPage,
                            filter = filter
                        )
                    }
                }.flow
            }
    }

    override suspend fun getSearchCapabilities(
        provider: ProviderType,
        type: MediaType
    ): SearchCapabilities {

        return resolveProvider(provider)
            .getSearchCapabilities(type)
    }

    // =========================================================
    // DETAILS
    // =========================================================

    override fun getMediaDetails(
        id: Int,
        mediaType: MediaType
    ): Flow<DataResult<MediaDetails>> {

        return offlineFirstFlow(

            identifier = "MediaDetails:$id:$mediaType",

            getCached = { provider ->
                database.mediaDao()
                    .observeDetails(
                        id = id,
                        provider = provider
                    )
                    .firstOrNull()
            },

            getUpdatedAt = { it.updatedAt },

            toDomain = { it.toDomain() },

            fetchNetwork = { source ->
                source.getMediaDetails(id, mediaType)
            },

            saveToDb = { details, _ ->

                database.useWriterConnection {

                    database.mediaDao()
                        .upsertDetails(details.toEntity())

                    database.mediaDao()
                        .upsert(details.toBaseEntity())
                }
            }
        )
    }

    override suspend fun refreshMediaDetails(
        id: Int,
        mediaType: MediaType
    ): ApiResult<Unit> {

        val (_, source) = getActiveProviderInfo()

        return when (
            val result = source.getMediaDetails(id, mediaType)
        ) {

            is ApiResult.Success -> {

                database.useWriterConnection {

                    database.mediaDao()
                        .upsertDetails(result.data.toEntity())

                    database.mediaDao()
                        .upsert(result.data.toBaseEntity())
                }

                ApiResult.Success(Unit)
            }

            is ApiResult.Error -> ApiResult.Error(result.error)

            is ApiResult.Empty -> {
                ApiResult.Empty("Refresh failed")
            }

            is ApiResult.Loading -> {
                ApiResult.Loading()
            }
        }
    }

    // =========================================================
    // HERO
    // =========================================================

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getHeroBanner(
        mediaType: MediaType,
        limit: Int
    ): Flow<List<Media>> {

        return activeProviderFlow.flatMapLatest { providerType ->

            val source = resolveProvider(providerType)

            channelFlow {

                launch {

                    try {

                        val lastUpdated =
                            database.remoteKeysDao()
                                .getMediaLastUpdated(
                                    provider = providerType,
                                    mediaType = mediaType,
                                    category = "HERO"
                                ) ?: 0L

                        if (
                            System.currentTimeMillis() - lastUpdated < homeTtl
                        ) {
                            return@launch
                        }

                        when (
                            val result = source.getHeroMediaList(
                                mediaType = mediaType,
                                limit = limit
                            )
                        ) {

                            is ApiResult.Success -> {

                                val entities =
                                    result.data.map {
                                        it.toEntity()
                                    }

                                val keys =
                                    result.data.mapIndexed { index, item ->

                                        MediaRemoteKeys(
                                            mediaId = item.id,
                                            provider = providerType,
                                            mediaType = mediaType,
                                            category = "HERO",
                                            sortOrder = index,
                                            prevPage = null,
                                            nextPage = null,
                                            lastUpdated = System.currentTimeMillis()
                                        )
                                    }

                                database.useWriterConnection {

                                    database.remoteKeysDao()
                                        .clearMediaKeys(
                                            providerType,
                                            mediaType,
                                            "HERO"
                                        )

                                    database.mediaDao()
                                        .upsertAll(entities)

                                    database.remoteKeysDao()
                                        .upsertMediaKeys(keys)
                                }
                            }

                            else -> Unit
                        }

                    } catch (_: Exception) {
                    }
                }

                database.mediaDao()
                    .observeMediaByCategory(
                        category = "HERO",
                        provider = providerType,
                        mediaType = mediaType,
                        limit = limit
                    )
                    .collect { entities ->

                        send(
                            entities.map {
                                it.toDomain()
                            }
                        )
                    }
            }
        }
    }

    // =========================================================
    // EPISODES / CHAPTERS
    // =========================================================

    @OptIn(
        ExperimentalCoroutinesApi::class,
        ExperimentalPagingApi::class
    )
    override fun getEpisodes(
        mediaId: Int
    ): Flow<PagingData<Episode>> {

        return activeProviderFlow.flatMapLatest { providerType ->

            val source = resolveProvider(providerType)

            Pager(
                config = PagingConfig(
                    pageSize = pageSize,
                    enablePlaceholders = false
                ),

                remoteMediator = EpisodeRemoteMediator(
                    mediaId = mediaId,
                    currentProviderType = providerType,
                    provider = source,
                    database = database,
                    cacheTimeoutMillis = settings.preferences.value.episodeTimeout
                ),

                pagingSourceFactory = {
                    database.episodeDao()
                        .getEpisodesByMedia(
                            mediaId,
                            providerType
                        )
                }
            ).flow.map { paging ->
                paging.map { it.toDomain() }
            }
        }
    }

    @OptIn(
        ExperimentalCoroutinesApi::class,
        ExperimentalPagingApi::class
    )
    override fun getChapters(
        mediaId: Int
    ): Flow<PagingData<Chapter>> {

        return activeProviderFlow.flatMapLatest { providerType ->

            val source = resolveProvider(providerType)

            Pager(
                config = PagingConfig(
                    pageSize = pageSize,
                    enablePlaceholders = false
                ),

                remoteMediator = ChapterRemoteMediator(
                    mediaId = mediaId,
                    currentProviderType = providerType,
                    provider = source,
                    database = database,
                    cacheTimeoutMillis = settings.preferences.value.chapterTimeout
                ),

                pagingSourceFactory = {
                    database.chapterDao()
                        .getChaptersByMedia(
                            mediaId,
                            providerType
                        )
                }
            ).flow.map { paging ->
                paging.map { it.toDomain() }
            }
        }
    }

    // =========================================================
    // RECOMMENDATIONS
    // =========================================================

    override fun getRecommendations(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<PagingData<Media>> {

        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize,
                prefetchDistance = 5
            )
        ) {

            GenericPagingSource { page ->

                getActiveSource().getRecommendations(
                    mediaId = mediaId,
                    mediaType = mediaType,
                    page = page
                )
            }
        }.flow
    }

    // =========================================================
    // REVIEWS
    // =========================================================

    override fun getReviews(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<PagingData<Review>> {

        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize,
                prefetchDistance = 5
            )
        ) {

            GenericPagingSource { page ->

                getActiveSource().getReviews(
                    mediaId = mediaId,
                    mediaType = mediaType,
                    page = page,
                    perPage = pageSize
                )
            }
        }.flow
    }
}












