package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.toMangaDomain
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ErrorType
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.domain.provider.MangaListProvider

class AniListMangaProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : MangaListProvider {

    override suspend fun getMangaList(category: MangaCategory, page: Int, perPage: Int): ApiResult<PageResult<Manga>> {
        if (!supportsCategory(category)) {
            return ApiResult.Error(
                ApiError(
                    type = ErrorType.SERVER_ERROR,
                    message = "Category not supported",
                    recoverable = false
                )
            )
        }
        return parser.safeApiCall(
            apiCall = {
                api.fetchMangaList(category = category, page = page, perPage = perPage)
            },
            transform = { data ->
                data.page?.let { pageData ->
                    PageResult(
                        items = pageData.media?.map { it.toMangaDomain() } ?: emptyList(),
                        currentPage = pageData.pageInfo?.currentPage ?: 1,
                        hasNextPage = pageData.pageInfo?.hasNextPage ?: false,
                        totalPages = pageData.pageInfo?.lastPage ?: 1
                    )
                }
            }
        )
    }

    override fun supportsCategory(category: MangaCategory): Boolean {
        return true
    }
}


