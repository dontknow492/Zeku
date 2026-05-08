package com.ghost.zeku.data.remote.mal.providers

import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.data.remote.mal.toMediaDomain
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.search.MediaSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities
import com.ghost.zeku.domain.model.search.SearchSort
import com.ghost.zeku.domain.provider.MediaSearchProvider

class MalSearchProvider(
    private val api: MalApi,
    private val parser: MalResponseParser
) : MediaSearchProvider {

    // =========================================================
    // MAL GENRE MAP
    // =========================================================

    private val malGenreMap = mapOf(
        "Action" to 1,
        "Adventure" to 2,
        "Comedy" to 4,
        "Drama" to 8,
        "Fantasy" to 10,
        "Horror" to 14,
        "Mystery" to 7,
        "Romance" to 22,
        "Sci-Fi" to 24,
        "Slice of Life" to 36,
        "Sports" to 30,
        "Supernatural" to 37
    )

    // =========================================================
    // CAPABILITIES
    // =========================================================

    override suspend fun getSearchCapabilities(
        mediaType: MediaType
    ): SearchCapabilities {

        return SearchCapabilities(

            supportsGenres = true,

            supportsTags = false, // MAL has no real tag system

            supportsYear = false,

            supportsSeason = mediaType == MediaType.ANIME,

            supportedFormats = when (mediaType) {

                MediaType.ANIME -> listOf(
                    MediaFormat.TV,
                    MediaFormat.MOVIE,
                    MediaFormat.OVA,
                    MediaFormat.ONA,
                    MediaFormat.SPECIAL
                )

                MediaType.MANGA -> listOf(
                    MediaFormat.MANGA,
                    MediaFormat.NOVEL,
                    MediaFormat.ONE_SHOT
                )

                MediaType.UNKNOWN -> emptyList()
            },

            supportedStatus = listOf(
                MediaReleaseStatus.FINISHED,
                MediaReleaseStatus.RELEASING,
                MediaReleaseStatus.NOT_YET_RELEASED
            ),

            supportedSorts = listOf(
                SearchSort.POPULARITY_DESC,
                SearchSort.SCORE_DESC,
                SearchSort.START_DATE_DESC
            ),

            availableGenres = malGenreMap.keys.toList(),

            availableTags = emptyList()
        )
    }

    // =========================================================
    // SEARCH
    // =========================================================

    override suspend fun searchMedia(
        query: String?,
        page: Int,
        perPage: Int,
        filter: MediaSearchFilter
    ): ApiResult<PageResult<Media>> {

        val mediaType = filter.mediaType ?: MediaType.ANIME

        return parser.safeApiCall(

            apiCall = {

                // -----------------------------------------------------
                // GENRES
                // -----------------------------------------------------

                val genreIds = filter.includedGenres
                    .mapNotNull { malGenreMap[it] }
                    .joinToString(",")

                    .takeIf { it.isNotBlank() }

                // -----------------------------------------------------
                // STATUS
                // -----------------------------------------------------

                val statusString = when (filter.status) {

                    MediaReleaseStatus.FINISHED -> {
                        if (mediaType == MediaType.ANIME) {
                            "finished_airing"
                        } else {
                            "finished"
                        }
                    }

                    MediaReleaseStatus.RELEASING -> {
                        if (mediaType == MediaType.ANIME) {
                            "currently_airing"
                        } else {
                            "currently_publishing"
                        }
                    }

                    MediaReleaseStatus.NOT_YET_RELEASED -> {
                        if (mediaType == MediaType.ANIME) {
                            "not_yet_aired"
                        } else {
                            "not_yet_published"
                        }
                    }

                    else -> null
                }

                // -----------------------------------------------------
                // API
                // -----------------------------------------------------

                api.searchMedia(
                    mediaType = mediaType,

                    query = query
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?: "",

                    page = page,

                    limit = perPage,

                    status = statusString,

                    genres = genreIds
                )
            },

            // =====================================================
            // TRANSFORM
            // =====================================================

            transform = { response ->

                PageResult(

                    items = response.data
                        ?.mapNotNull {
                            it.toMediaDomain(mediaType)
                        }
                        ?: emptyList(),

                    currentPage = page,

                    hasNextPage = response.paging?.nextUrl != null,

                    totalPages = null
                )
            }
        )
    }
}