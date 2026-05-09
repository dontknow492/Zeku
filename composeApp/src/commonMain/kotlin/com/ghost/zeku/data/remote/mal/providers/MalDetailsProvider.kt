package com.ghost.zeku.data.remote.mal.providers

import com.ghost.zeku.data.remote.jikan.JikanApi
import com.ghost.zeku.data.remote.jikan.toDomain
import com.ghost.zeku.data.remote.jikan.toMediaDomain
import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.data.remote.mal.toMediaDetailsDomain
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.MediaDetails
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.media.Review
import com.ghost.zeku.domain.provider.MediaDetailsProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class MalDetailsProvider(
    private val malApi: MalApi,
    private val jikanApi: JikanApi, // Injected!
    private val parser: MalResponseParser
) : MediaDetailsProvider {
    override suspend fun getMediaDetails(
        id: Int,
        mediaType: MediaType
    ): ApiResult<MediaDetails> {
        return parser.safeApiCall(
            apiCall = {
                // Fetch Official Data and Unofficial Characters AT THE SAME TIME
                coroutineScope {
                    val malDeferred = async {
                        malApi.getMediaDetails(mediaType = mediaType, id = id)
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
                    malData.toMediaDetailsDomain(mediaType = mediaType, chars)
                }
            },
            transform = { it } // Mapping is handled directly inside the apiCall block
        )
    }

    override suspend fun getRecommendations(
        mediaId: Int,
        mediaType: MediaType,
        page: Int
    ): ApiResult<PageResult<Media>> {
        if (page > 1) return ApiResult.Success(PageResult(emptyList(), page, false, 1))

        return parser.safeApiCall(
            apiCall = {
                when (mediaType) {
                    MediaType.ANIME -> jikanApi.getAnimeRecommendations(mediaId)
                    MediaType.MANGA -> jikanApi.getMangaRecommendations(mediaId)
                    MediaType.UNKNOWN -> TODO()
                }
            },
            transform = { response ->
                PageResult(
                    items = response.data.mapNotNull { it.toMediaDomain(mediaType) },
                    currentPage = 1,
                    hasNextPage = false,
                    totalPages = 1
                )
            }
        )
    }

    override suspend fun getReviews(
        mediaId: Int,
        mediaType: MediaType,
        page: Int,
        perPage: Int
    ): ApiResult<PageResult<Review>> {
        return parser.safeApiCall(
            apiCall = { jikanApi.getAnimeReviews(mediaId, page) },
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