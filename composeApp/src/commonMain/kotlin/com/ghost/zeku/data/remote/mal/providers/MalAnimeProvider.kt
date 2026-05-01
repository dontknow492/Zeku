package com.ghost.zeku.data.remote.mal.providers

import com.ghost.zeku.data.remote.jikan.JikanApi
import com.ghost.zeku.data.remote.jikan.toAnimeDomain
import com.ghost.zeku.data.remote.jikan.toDomain
import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.data.remote.mal.toAnimeDetailsDomain
import com.ghost.zeku.data.remote.mal.toAnimeDomain
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaStatus
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.search.AnimeSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities
import com.ghost.zeku.domain.model.search.SearchSort
import com.ghost.zeku.domain.provider.AnimeDetailsProvider
import com.ghost.zeku.domain.provider.AnimeListProvider
import com.ghost.zeku.domain.provider.AnimeSearchProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class MalAnimeProvider(
    private val api: MalApi,
    private val parser: MalResponseParser
) : AnimeListProvider {

    override suspend fun getAnimeList(category: AnimeCategory, page: Int, perPage: Int): ApiResult<PageResult<Anime>> {
        if (!supportsCategory(category)) {
            return ApiResult.Error(
                error = ApiError(
                    type = com.ghost.zeku.domain.model.api.ErrorType.SERVER_ERROR,
                    message = "Category not supported",
                    recoverable = false
                ),
                recoverable = false
            )
        }
        return parser.safeApiCall(
            apiCall = {
                api.fetchAnimeList(category = category, page = page, limit = perPage)
            },
            transform = { response ->
                PageResult(
                    items = response.data?.mapNotNull { it.toAnimeDomain() } ?: emptyList(),
                    currentPage = page,
                    hasNextPage = response.paging?.nextUrl != null,
                    totalPages = null // MAL doesn't provide total pages in the response
                )
            }
        )
    }

    override fun supportsCategory(category: AnimeCategory): Boolean {
        // MAL supports Seasonal via a custom endpoint, and others via Ranking
        return category == AnimeCategory.SEASONAL || category.toMalRankingType() != null
    }

    override suspend fun getAnimeHeroList(limit: Int): ApiResult<List<Anime>> {
        return parser.safeApiCall(
            apiCall = {
                api.fetchAnimeHeroList(limit)
            },
            transform = { response ->
                response.data?.mapNotNull { it.toAnimeDomain() } ?: emptyList()
            }
        )
    }
}

class MalAnimeSearchProvider(
    private val api: MalApi,
    private val parser: MalResponseParser
) : AnimeSearchProvider {

    // Simple map to translate our unified String genres into MAL's specific integer IDs
    private val malGenreMap = mapOf(
        "Action" to 1, "Adventure" to 2, "Comedy" to 4, "Drama" to 8,
        "Fantasy" to 10, "Horror" to 14, "Mystery" to 7, "Romance" to 22,
        "Sci-Fi" to 24, "Slice of Life" to 36, "Sports" to 30, "Supernatural" to 37
    )

    override suspend fun getAnimeSearchCapabilities(): SearchCapabilities {
        return SearchCapabilities(
            supportsGenres = true,
            supportsTags = false, // MAL has no concept of AniList-style tags
            supportsYear = false, // Standard search endpoint doesn't support year filtering
            supportsSeason = false,

            // MAL only reliably filters these specific formats in search
            supportedFormats = listOf(
                MediaFormat.TV, MediaFormat.MOVIE, MediaFormat.OVA, MediaFormat.SPECIAL
            ),
            supportedStatus = listOf(
                MediaReleaseStatus.RELEASING, MediaReleaseStatus.FINISHED
            ),

            // MAL search only supports a few sorts, it relies on /ranking for the rest
            supportedSorts = listOf(
                SearchSort.SCORE_DESC, SearchSort.ALPHABETICAL_ASC
            ),
            availableGenres = listOf("Action", "Adventure", "Comedy" /* ... */)
        )
    }

    override suspend fun searchAnime(
        query: String?,
        page: Int,
        perPage: Int,
        filter: AnimeSearchFilter
    ): ApiResult<PageResult<Anime>> {
        return parser.safeApiCall(
            apiCall = {

                // Translate list of string genres to a comma-separated list of IDs
                val genreIds = filter.includedGenres
                    .mapNotNull { malGenreMap[it] }
                    .joinToString(",")
                    .takeIf { it.isNotEmpty() }

                // Translate our MediaStatus enum to MAL's required strings
                val statusString = when (filter.status) {
                    MediaReleaseStatus.FINISHED -> "completed"
                    MediaReleaseStatus.RELEASING -> "currently_airing"
                    MediaReleaseStatus.NOT_YET_RELEASED -> "not_yet_aired"
                    else -> null
                }

                api.searchAnime(
                    query = query?.takeIf { it.isNotBlank() } ?: "",
                    page = page,
                    limit = perPage,
                    status = statusString,
                    genres = genreIds,

                    )
            },
            transform = { response ->
                PageResult(
                    items = response.data?.mapNotNull { it.toAnimeDomain() } ?: emptyList(),
                    currentPage = page,
                    hasNextPage = response.paging?.nextUrl != null,
                    totalPages = null
                )
            }
        )
    }
}

class MalAnimeDetailsProvider(
    private val malApi: MalApi,
    private val jikanApi: JikanApi, // Injected!
    private val parser: MalResponseParser
) : AnimeDetailsProvider {

    override suspend fun getAnimeDetails(id: Int): ApiResult<AnimeDetails> {
        return parser.safeApiCall(
            apiCall = {
                // Fetch Official Data and Unofficial Characters AT THE SAME TIME
                coroutineScope {
                    val malDeferred = async {
                        malApi.getAnimeDetails(id = id)
                    }
                    val jikanDeferred = async {
                        // Wrap in try-catch so Jikan being down doesn't crash the core MAL load
                        try {
                            jikanApi.getAnimeCharacters(id).data
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val malData = malDeferred.await()
                    val jikanChars = jikanDeferred.await() ?: emptyList()

                    val chars = jikanChars.mapNotNull { jikanChar -> jikanChar.toDomain() }

                    // Combine them!
                    malData.toAnimeDetailsDomain(chars)
                }
            },
            transform = { it } // Mapping is handled directly inside the apiCall block
        )
    }

    override suspend fun getAnimeEpisodes(id: Int, page: Int, perPage: Int): ApiResult<PageResult<Episode>> {
        return parser.safeApiCall(
            apiCall = { jikanApi.getAnimeEpisodes(id, page) },
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

    override suspend fun getAnimeRecommendations(id: Int, page: Int): ApiResult<PageResult<Anime>> {
        // Jikan Recommendations are not paginated, so if page > 1, we return empty to stop the Pager.
        if (page > 1) return ApiResult.Success(PageResult(emptyList(), page, false, 1))

        return parser.safeApiCall(
            apiCall = { jikanApi.getAnimeRecommendations(id) },
            transform = { response ->
                PageResult(
                    items = response.data.mapNotNull { it.toAnimeDomain() },
                    currentPage = 1,
                    hasNextPage = false,
                    totalPages = 1
                )
            }
        )
    }

    override suspend fun getAnimeReviews(id: Int, page: Int, perPage: Int): ApiResult<PageResult<Review>> {
        return parser.safeApiCall(
            apiCall = { jikanApi.getAnimeReviews(id, page) },
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


fun AnimeCategory.toMalRankingType(): String? {
    return when (this) {
        AnimeCategory.TOP_RATED -> "all"         // Top Anime Series
        AnimeCategory.TRENDING -> "airing"       // Currently airing
        AnimeCategory.UPCOMING -> "upcoming"     // Top upcoming
        AnimeCategory.POPULAR -> "bypopularity"  // Most members
        AnimeCategory.MOVIE -> "movie"           // Top movies
        AnimeCategory.SEASONAL -> null           // Requires a different endpoint
    }
}