package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.model.GraphQLRequest
import com.ghost.zeku.data.remote.anilist.toAniListSort
import com.ghost.zeku.data.remote.anilist.toMangaDomain
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
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
            supportsGenres = true,
            supportsTags = true,
            supportsYear = true, // Users can search by publishing start year
            supportsSeason = false, // Manga does not have Winter/Spring broadcast seasons!

            supportedFormats = listOf(
                MediaFormat.MANGA,
                MediaFormat.NOVEL,
                MediaFormat.ONE_SHOT
            ),
            supportedStatus = listOf(
                MediaReleaseStatus.FINISHED,
                MediaReleaseStatus.RELEASING,
                MediaReleaseStatus.NOT_YET_RELEASED,
                MediaReleaseStatus.HIATUS,
                MediaReleaseStatus.CANCELLED
            ),

            supportedSorts = SearchSort.entries, // Supports all standard sorting (Trending, Score, etc.)

            // In a real app, you might fetch these from a database or a one-time API call,
            // but hardcoding the standard list is perfectly fine and much faster.
            availableGenres = listOf(
                "Action",
                "Adventure",
                "Comedy",
                "Drama",
                "Fantasy",
                "Horror",
                "Mecha",
                "Mystery",
                "Psychological",
                "Romance",
                "Sci-Fi",
                "Slice of Life",
                "Sports",
                "Supernatural",
                "Thriller"
            ),
            availableTags = listOf(
                "Anti-Hero",
                "Dark Fantasy",
                "Demons",
                "Isekai",
                "Magic",
                "Revenge",
                "Swordplay",
                "Tragedy",
                "Vampires",
                "Zombies"
            ) // Usually you'd include the top 50-100 AniList tags here
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


