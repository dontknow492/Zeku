package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.toAnimeDomain
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.provider.AnimeListProvider

class AniListAnimeProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : AnimeListProvider {

    override suspend fun getAnimeList(category: AnimeCategory, page: Int, perPage: Int): ApiResult<PageResult<Anime>> {
        return parser.safeApiCall(
            apiCall = {
                api.fetchAnimeList(
                    category = category,
                    page = page,
                    perPage = perPage,
                )
            },
            transform = { data ->
                data.page?.let { pageData ->
                    PageResult(
                        items = pageData.media?.map { it.toAnimeDomain() } ?: emptyList(),
                        currentPage = pageData.pageInfo?.currentPage ?: 1,
                        hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
                        totalPages = pageData.pageInfo?.lastPage ?: 1
                    )
                }
            }
        )
    }

    override fun supportsCategory(category: AnimeCategory): Boolean {
        // AniList is powerful enough to support all current categories
        return true
    }
}


