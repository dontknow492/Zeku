package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.model.GraphQLRequest
import com.ghost.zeku.data.remote.anilist.toAniListSort
import com.ghost.zeku.data.remote.anilist.toMangaDomain
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.search.MangaSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities
import com.ghost.zeku.domain.model.search.SearchSort
import com.ghost.zeku.domain.provider.MangaSearchProvider

class AniListMangaSearchProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : MangaSearchProvider {

    override suspend fun getMangaSearchCapabilities(): SearchCapabilities {
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
            supportsFormatFilter = true, // To separate MANGA vs NOVEL vs ONE_SHOT
            supportsStatusFilter = true,
            supportsYearFilter = false, // Manga doesn't usually use "Seasons/Years" like anime does
            supportsSeasonFilter = false,
            supportsExclusion = true,
            supportedSorts = SearchSort.entries
        )
    }

    override suspend fun searchManga(
        query: String?,
        page: Int,
        perPage: Int,
        filter: MangaSearchFilter
    ): ApiResult<PageResult<Manga>> {

        val sortString = filter.sort.toAniListSort()

        val variables = GraphQLRequest.Variables(
            page = page,
            perPage = perPage,
            search = query?.takeIf { it.isNotBlank() },
            sort = listOf(sortString),
            format = filter.format?.name,
            status = filter.status?.name,
            genreIn = filter.includedGenres.takeIf { it.isNotEmpty() },
            genreNotIn = filter.excludedGenres.takeIf { it.isNotEmpty() },
            tagIn = filter.includedTags.takeIf { it.isNotEmpty() },
            tagNotIn = filter.excludedTags.takeIf { it.isNotEmpty() },
            isAdult = false
        )

        return parser.safeApiCall(
            apiCall = { api.searchManga(variables) },
            transform = { data ->
                data.page?.let { pageData ->
                    PageResult(
                        items = pageData.media?.map { it.toMangaDomain() } ?: emptyList(),
                        currentPage = pageData.pageInfo?.currentPage ?: 1,
                        hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
                        totalPages = pageData.pageInfo?.lastPage ?: 1
                    )
                }
            }
        )
    }
}


