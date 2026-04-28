package com.ghost.zeku.data.remote.mal.providers


import com.ghost.zeku.data.remote.jikan.JikanApi
import com.ghost.zeku.data.remote.jikan.toDomain
import com.ghost.zeku.data.remote.jikan.toMangaDomain
import com.ghost.zeku.data.remote.mal.*
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ErrorType
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.enum.MediaStatus
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.search.MangaSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities
import com.ghost.zeku.domain.model.search.SearchSort
import com.ghost.zeku.domain.provider.MangaDetailsProvider
import com.ghost.zeku.domain.provider.MangaListProvider
import com.ghost.zeku.domain.provider.MangaSearchProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MalMangaProvider(
    private val api: MalApi,
    private val parser: MalResponseParser
) : MangaListProvider {

    override suspend fun getMangaList(category: MangaCategory, page: Int, perPage: Int): ApiResult<PageResult<Manga>> {
        if (!supportsCategory(category)) {
            return ApiResult.Error(
                ApiError(
                    type = ErrorType.SERVER_ERROR,
                    message = "Category not supported",
                    recoverable = false
                )
            )
        }

        return parser.safeApiCall(
            apiCall = {
                api.fetchMangaList(category = category, page = page, limit = 20)
            },
            transform = { response ->
                PageResult(
                    items = response.data?.mapNotNull { it.toMangaDomain() } ?: emptyList(),
                    currentPage = page,
                    hasNextPage = response.paging?.nextUrl != null,
                    totalPages = null
                )
            }
        )
    }

    override fun supportsCategory(category: MangaCategory): Boolean {
        // Returns true only if a valid MAL ranking type exists for this category
        return category.toMalRankingType() != null
    }

    override suspend fun getMangaHeroList(limit: Int): ApiResult<List<Manga>> {
        return parser.safeApiCall(
            apiCall = {
                api.fetchMangaHeroList(limit)
            },
            transform = { response ->
                response.data?.mapNotNull { it.toMangaDomain() } ?: emptyList()
            }
        )
    }
}

class MalMangaSearchProvider(
    private val api: MalApi,
    private val parser: MalResponseParser
) : MangaSearchProvider {

    private val malGenreMap = mapOf(
        "Action" to 1, "Adventure" to 2, "Comedy" to 4, "Drama" to 8,
        "Fantasy" to 10, "Horror" to 14, "Mystery" to 7, "Romance" to 22,
        "Sci-Fi" to 24, "Slice of Life" to 36, "Sports" to 30, "Supernatural" to 37
    )

    override suspend fun getMangaSearchCapabilities(): SearchCapabilities {
        return SearchCapabilities(
            supportedGenres = malGenreMap.keys.toList(),
            supportsFormatFilter = false,
            supportsStatusFilter = true,
            supportsYearFilter = false,
            supportsSeasonFilter = false,
            supportsExclusion = false,
            supportedSorts = listOf(SearchSort.ALPHABETICAL_ASC)
        )
    }

    override suspend fun searchManga(
        query: String?,
        page: Int,
        perPage: Int,
        filter: MangaSearchFilter
    ): ApiResult<PageResult<Manga>> {
        return parser.safeApiCall(
            apiCall = {

                val genreIds = filter.includedGenres
                    .mapNotNull { malGenreMap[it] }
                    .joinToString(",")
                    .takeIf { it.isNotEmpty() }

                val statusString = when (filter.status) {
                    MediaStatus.FINISHED -> "completed"
                    MediaStatus.RELEASING -> "currently_publishing"
                    MediaStatus.NOT_YET_RELEASED -> "not_yet_published"
                    MediaStatus.HIATUS -> "on_hiatus"
                    else -> null
                }

                api.searchManga(
                    query = query?.takeIf { it.isNotBlank() } ?: "",
                    page = page,
                    limit = perPage,
                    status = statusString,
                    genres = genreIds,

                    )
            },
            transform = { response ->
                PageResult(
                    items = response.data?.mapNotNull { it.toMangaDomain() } ?: emptyList(),
                    currentPage = page,
                    hasNextPage = response.paging?.nextUrl != null,
                    totalPages = null
                )
            }
        )
    }
}

class MalMangaDetailsProvider(
    private val malApi: MalApi,
    private val jikanApi: JikanApi,
    private val parser: MalResponseParser
) : MangaDetailsProvider {

    override suspend fun getMangaDetails(id: Int): ApiResult<MangaDetails> {
        return parser.safeApiCall(
            apiCall = {
                coroutineScope {
                    val malDeferred = async {
                        malApi.getMangaDetails(id = id)
                    }
                    val jikanDeferred = async {
                        try {
                            jikanApi.getMangaCharacters(id).data
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val malData = malDeferred.await()
                    val jikanChars = jikanDeferred.await()

                    malData.toMangaDetailsDomain(jikanChars)
                }
            },
            transform = { it }
        )
    }

    override suspend fun getMangaChapters(id: Int, page: Int, perPage: Int): ApiResult<PageResult<Chapter>> {
        // Jikan does not provide a robust manga chapter list endpoint.
        // Returning empty allows the UI to gracefully hide the chapter list,
        // or prepares the way to inject MangaDex here later.
        return ApiResult.Success(PageResult(emptyList(), 1, false, 1))
    }

    override suspend fun getMangaRecommendations(id: Int, page: Int): ApiResult<PageResult<Manga>> {
        if (page > 1) return ApiResult.Success(PageResult(emptyList(), page, false, 1))

        return parser.safeApiCall(
            apiCall = { jikanApi.getMangaRecommendations(id) },
            transform = { response ->
                PageResult(
                    items = response.data.mapNotNull { it.toMangaDomain() },
                    currentPage = 1,
                    hasNextPage = false,
                    totalPages = 1
                )
            }
        )
    }

    override suspend fun getMangaReviews(id: Int, page: Int, perPage: Int): ApiResult<PageResult<Review>> {
        return parser.safeApiCall(
            apiCall = { jikanApi.getMangaReviews(id, page) },
            transform = { response ->
                PageResult(
                    items = response.data.map { it.toDomain() },
                    currentPage = page,
                    hasNextPage = response.pagination?.has_next_page ?: false,
                    totalPages = response.pagination?.last_visible_page ?: page
                )
            }
        )
    }
}