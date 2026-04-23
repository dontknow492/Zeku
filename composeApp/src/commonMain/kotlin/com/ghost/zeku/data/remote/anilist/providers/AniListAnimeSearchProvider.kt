package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.model.GraphQLRequest
import com.ghost.zeku.data.remote.anilist.toAniListSort
import com.ghost.zeku.data.remote.anilist.toAnimeDomain
import com.ghost.zeku.data.repository.AniListAuthRepositoryImpl
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.search.AnimeSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities
import com.ghost.zeku.domain.model.search.SearchSort
import com.ghost.zeku.domain.provider.AnimeSearchProvider

class AniListAnimeSearchProvider(
    private val api: AniListApi,
    private val authRepository: AniListAuthRepositoryImpl,
    private val parser: AniListResponseParser
) : AnimeSearchProvider {

    // AniList is the gold standard, it supports everything!
    override suspend fun getAnimeSearchCapabilities(): SearchCapabilities {
        return SearchCapabilities(
            supportedGenres = listOf(
                "Action",
                "Adventure",
                "Comedy",
                "Drama",
                "Ecchi",
                "Fantasy",
                "Horror",
                "Mahou Shoujo",
                "Mecha",
                "Music",
                "Mystery",
                "Psychological",
                "Romance",
                "Sci-Fi",
                "Slice of Life",
                "Sports",
                "Supernatural",
                "Thriller"
            ),
            supportsFormatFilter = true,
            supportsStatusFilter = true,
            supportsYearFilter = true,
            supportsSeasonFilter = true,
            supportsExclusion = true,
            supportedSorts = SearchSort.entries
        )
    }

    override suspend fun searchAnime(
        query: String?,
        page: Int,
        perPage: Int,
        filter: AnimeSearchFilter
    ): ApiResult<PageResult<Anime>> {

        // 1. Map generic Sort Enum to AniList Sort String
        val sortString = filter.sort.toAniListSort()


        // 2. Build the Translation Object
        val variables = GraphQLRequest.Variables(
            page = page,
            perPage = perPage,
            search = query?.takeIf { it.isNotBlank() },
            sort = listOf(sortString),
            seasonYear = filter.year,
            season = filter.season?.name, // Kotlin enum name matches AniList perfectly!
            format = filter.format?.name,
            status = filter.status?.name,

            // Only send arrays if they aren't empty to save bandwidth
            genreIn = filter.includedGenres.takeIf { it.isNotEmpty() },
            genreNotIn = filter.excludedGenres.takeIf { it.isNotEmpty() },
            tagIn = filter.includedTags.takeIf { it.isNotEmpty() },
            tagNotIn = filter.excludedTags.takeIf { it.isNotEmpty() },

            isAdult = false // Keeps the search safe by default
        )

        // 3. Execute
        return parser.safeApiCall(
            apiCall = { api.searchAnime(variables, token = authRepository.getAccessToken()) },
            transform = { data ->
                data.page?.let { pageData ->
                    PageResult(
                        items = pageData.media?.map { it.toAnimeDomain() } ?: emptyList(),
                        currentPage = pageData.pageInfo?.currentPage ?: 1,
                        hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
                        totalPages = pageData.pageInfo?.lastPage ?: 1
                    )
                }
            }
        )
    }
}

