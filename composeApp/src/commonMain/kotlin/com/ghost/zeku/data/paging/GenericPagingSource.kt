package com.ghost.zeku.data.paging


import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.getErrorMessage
import com.ghost.zeku.domain.model.media.PageResult

/**
 * A highly reusable PagingSource for online-only data like Search results.
 * It takes a suspending lambda so it doesn't care whether it is fetching Anime, Manga, or Staff.
 */
class GenericPagingSource<T : Any>(
    private val fetchFn: suspend (page: Int) -> ApiResult<PageResult<T>>
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1

        return when (val response = fetchFn(page)) {
            is ApiResult.Success -> {
                LoadResult.Page(
                    data = response.data.items,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.data.hasNextPage) page + 1 else null
                )
            }

            is ApiResult.Error -> {
                LoadResult.Error(response.error.cause ?: Exception(response.getErrorMessage()))
            }

            is ApiResult.Empty -> {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = null
                )
            }

            is ApiResult.Loading -> {
                LoadResult.Error(IllegalStateException("Unexpected Loading state inside PagingSource"))
            }
        }
    }
}