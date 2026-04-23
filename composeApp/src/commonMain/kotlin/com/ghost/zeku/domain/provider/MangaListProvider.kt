package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.PageResult


interface MangaListProvider {

    fun supportsCategory(category: MangaCategory): Boolean

    suspend fun getMangaList(
        category: MangaCategory,
        page: Int,
        perPage: Int = 20
    ): ApiResult<PageResult<Manga>>
}