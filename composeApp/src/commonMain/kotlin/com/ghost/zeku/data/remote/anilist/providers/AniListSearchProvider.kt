package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.*
import com.ghost.zeku.data.remote.anilist.model.GraphQLRequest
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.MediaFormat
import com.ghost.zeku.domain.model.media.MediaReleaseStatus
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.filter.search.MediaSearchFilter
import com.ghost.zeku.domain.model.filter.search.SearchCapabilities
import com.ghost.zeku.domain.model.filter.search.SearchSort
import com.ghost.zeku.domain.provider.MediaSearchProvider


class AniListSearchProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : MediaSearchProvider {
    override suspend fun getSearchCapabilities(
        mediaType: MediaType
    ): SearchCapabilities {

        return SearchCapabilities(

            supportsGenres = true,

            supportsTags = true,

            supportsYear = true,

            supportsSeason = mediaType == MediaType.ANIME,

            supportsCountry = true,

            supportsAdult = true,

            supportedFormats = when (mediaType) {
                MediaType.ANIME -> animeFormats
                MediaType.MANGA -> mangaFormats
                else -> emptyList()
            },

            supportedStatus = MediaReleaseStatus.entries,

            supportedSorts = SearchSort.entries,

            availableGenres = AniListConstants.GENRES,

            availableTags = AniListConstants.TAGS
        )
    }

    override suspend fun searchMedia(
        query: String?,
        page: Int,
        perPage: Int,
        filter: MediaSearchFilter
    ): ApiResult<PageResult<Media>> {
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
            apiCall = { api.searchMedia(variables = variables) },
            transform = { data ->
                data.page?.let { pageData ->
                    PageResult(
                        items = pageData.media?.map { it.toMediaDomain() } ?: emptyList(),
                        currentPage = pageData.pageInfo?.currentPage ?: 1,
                        hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
                        totalPages = pageData.pageInfo?.lastPage ?: 1
                    )
                }
            }
        )
    }


    private val animeFormats = listOf(
        MediaFormat.TV,
        MediaFormat.TV_SHORT,
        MediaFormat.MOVIE,
        MediaFormat.SPECIAL,
        MediaFormat.OVA,
        MediaFormat.ONA,
        MediaFormat.MUSIC
    )

    private val mangaFormats = listOf(
        MediaFormat.MANGA,
        MediaFormat.NOVEL,
        MediaFormat.ONE_SHOT
    )

}

