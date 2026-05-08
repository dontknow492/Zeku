package com.ghost.zeku.data.remote.mal.providers

import com.ghost.zeku.data.remote.jikan.JikanApi
import com.ghost.zeku.data.remote.jikan.toDomain
import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.Chapter
import com.ghost.zeku.domain.model.media.Episode
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.provider.MediaContentProvider

class MalMediaContentProvider(
    private val api: MalApi,
    private val jikanApi: JikanApi,
    private val parser: MalResponseParser
) : MediaContentProvider {
    override suspend fun getEpisodes(
        mediaId: Int,
        page: Int,
        perPage: Int
    ): ApiResult<PageResult<Episode>> {
        return parser.safeApiCall(
            apiCall = { jikanApi.getAnimeEpisodes(mediaId, page) },
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

    override suspend fun getChapters(
        mediaId: Int,
        page: Int,
        perPage: Int
    ): ApiResult<PageResult<Chapter>> {
        return ApiResult.Success(PageResult(emptyList(), 1, false, 1))
    }

}