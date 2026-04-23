package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.search.MangaSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities

interface MangaSearchProvider {
    suspend fun getMangaSearchCapabilities(): SearchCapabilities

    suspend fun searchManga(
        query: String?,
        page: Int,
        perPage: Int,
        filter: MangaSearchFilter
    ): ApiResult<PageResult<Manga>>
}

