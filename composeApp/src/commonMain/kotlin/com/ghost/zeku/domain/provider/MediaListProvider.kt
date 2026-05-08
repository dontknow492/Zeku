package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MediaCategory
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.PageResult


interface MediaListProvider {

    fun supportsCategory(
        category: MediaCategory,
        mediaType: MediaType
    ): Boolean

    val supportedCategories: Map<
            MediaType,
            Set<MediaCategory>
            >

    suspend fun getMediaList(
        category: MediaCategory,
        mediaType: MediaType,
        page: Int,
        perPage: Int = 20
    ): ApiResult<PageResult<Media>>

    suspend fun getHeroMediaList(
        mediaType: MediaType,
        limit: Int = 5
    ): ApiResult<List<Media>>
}