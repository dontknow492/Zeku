package com.ghost.zeku.data.remote.anilist

import com.ghost.zeku.data.remote.anilist.model.*
import com.ghost.zeku.domain.model.enum.MediaCategory
import com.ghost.zeku.domain.model.enum.MediaType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import zeku.composeApp.BuildConfig

class AniListApi(private val client: HttpClient) {
    private val baseUrl = BuildConfig.ANILIST_BASE_URL

    suspend fun fetchMediaList(
        mediaType: MediaType,
        category: MediaCategory,
        page: Int,
        perPage: Int = 20
    ): AniListResponse<AniListPageData> {
        return execute(
            query = AniListQueries.FETCH_MEDIA_LIST,
            vars = category.toAniListVariables(
                mediaType = mediaType,
                page = page,
                perPage = perPage
            )
        )
    }


    suspend fun fetchHeroMediaList(
        mediaType: MediaType,
        limit: Int
    ): AniListResponse<AniListPageData> {
        return execute(
            query = AniListQueries.FETCH_HERO_MEDIA,
            vars = GraphQLRequest.Variables(
                page = 1,
                perPage = limit,
                type = mediaType.name,
                sort = listOf("TRENDING_DESC"),
                status = "RELEASING",
                isAdult = false
            )
        )
    }

    suspend fun searchMedia(
        variables: GraphQLRequest.Variables
    ): AniListResponse<AniListPageData> {
        return execute(
            query = AniListQueries.SEARCH_MEDIA,
            vars = variables
        )
    }


    suspend fun getMediaDetails(
        id: Int,
        mediaType: MediaType
    ): AniListResponse<AniListSingleMediaData> {
        return execute(
            query = AniListQueries.GET_MEDIA_DETAILS,
            vars = GraphQLRequest.Variables(
                id = id,
                type = mediaType.name
            )
        )
    }

    suspend fun getMediaRecommendations(
        id: Int,
        page: Int
    ): AniListResponse<AniListRecommendationsResponse> {
        return execute(
            query = AniListQueries.GET_MEDIA_RECOMMENDATIONS,
            vars = GraphQLRequest.Variables(
                id = id,
                page = page
            )
        )
    }

    suspend fun getMediaReviews(
        id: Int,
        page: Int
    ): AniListResponse<AniListReviewsResponse> {
        return execute(
            query = AniListQueries.GET_MEDIA_REVIEWS,
            vars = GraphQLRequest.Variables(
                id = id,
                page = page
            )
        )
    }

    suspend fun getCurrentUser(): AniListResponse<ViewerWrapper> {
        return execute(
            query = AniListQueries.GET_USER_VIEWER,
            vars = GraphQLRequest.Variables()
        )
    }


    suspend fun saveMediaListEntry(vars: GraphQLRequest.Variables) =
        execute<AniListUpdateData>(AniListQueries.UPDATE_ENTRY, vars)

    suspend fun deleteMediaListEntry(vars: GraphQLRequest.Variables) =
        execute<AniListDeleteData>(AniListQueries.DELETE_ENTRY, vars)

    suspend fun getUserLibrary(vars: GraphQLRequest.Variables) =
        execute<AniListLibraryData>(AniListQueries.FETCH_LIBRARY, vars)

    suspend fun getMediaListEntry(vars: GraphQLRequest.Variables) =
        execute<AniListUpdateData>(AniListQueries.UPDATE_ENTRY, vars)


    private suspend inline fun <reified T> execute(
        query: String,
        vars: GraphQLRequest.Variables
    ): AniListResponse<T> {
        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(GraphQLRequest(query, vars))
        }.body()
    }


}