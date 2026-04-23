package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.PageResult

interface AnimeListProvider {

    /** Check if this provider can fulfill a request for a specific category */
    fun supportsCategory(category: AnimeCategory): Boolean


    suspend fun getAnimeList(
        category: AnimeCategory,
        page: Int,
        perPage: Int = 20
    ): ApiResult<PageResult<Anime>>
}


