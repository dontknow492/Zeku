package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.model.search.AnimeSearchFilter
import com.ghost.zeku.domain.model.search.SearchCapabilities

interface AnimeSearchProvider {
    /** * The UI calls this ONCE when the Search screen opens to configure the filter UI.
     */
    suspend fun getAnimeSearchCapabilities(): SearchCapabilities

    /**
     * The actual search call. Notice how 'query' is optional now,
     * because users might just want to search by Genre without typing a name!
     */
    suspend fun searchAnime(
        query: String?,
        page: Int,
        perPage: Int,
        filter: AnimeSearchFilter
    ): ApiResult<PageResult<Anime>>
}

