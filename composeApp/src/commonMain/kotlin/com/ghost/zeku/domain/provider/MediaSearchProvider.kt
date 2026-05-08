package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.search.MediaSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities

interface MediaSearchProvider {

    /**
     * Dynamically configure search UI.
     */
    suspend fun getSearchCapabilities(
        mediaType: MediaType
    ): SearchCapabilities

    /**
     * Unified search.
     */
    suspend fun searchMedia(
        query: String?,
        page: Int,
        perPage: Int,
        filter: MediaSearchFilter
    ): ApiResult<PageResult<Media>>
}