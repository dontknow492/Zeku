package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.Chapter
import com.ghost.zeku.domain.model.media.Episode
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.provider.MediaContentProvider

class AniListMediaContentProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : MediaContentProvider {
    override suspend fun getEpisodes(
        mediaId: Int,
        page: Int,
        perPage: Int
    ): ApiResult<PageResult<Episode>> {
        // NOTE: AniList does not natively provide a robust episode list.
        // Returning an empty result here is safe. Later, you can wrap a dedicated
        // scraping provider (like Consumet) in your Repository to fulfill this request!
        return ApiResult.Success(PageResult(emptyList(), 1, false, 1))
    }

    override suspend fun getChapters(
        mediaId: Int,
        page: Int,
        perPage: Int
    ): ApiResult<PageResult<Chapter>> {
        return ApiResult.Success(PageResult(emptyList(), 1, false, 1))
    }

}