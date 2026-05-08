package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.Chapter
import com.ghost.zeku.domain.model.media.Episode
import com.ghost.zeku.domain.model.media.PageResult

interface MediaContentProvider {

    suspend fun getEpisodes(
        mediaId: Int,
        page: Int,
        perPage: Int = 50
    ): ApiResult<PageResult<Episode>>

    suspend fun getChapters(
        mediaId: Int,
        page: Int,
        perPage: Int = 50
    ): ApiResult<PageResult<Chapter>>
}