package com.ghost.zeku.data.remote.anilist

import com.ghost.zeku.data.remote.anilist.model.*
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.MangaCategory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import zeku.composeApp.BuildConfig

class AniListApi(private val client: HttpClient) {
    private val baseUrl = BuildConfig.ANILIST_BASE_URL

    suspend fun fetchAnimeList(
        category: AnimeCategory,
        page: Int,
        perPage: Int = 20,
    ): AniListResponse<AniListPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.FETCH_ANIME_LIST,
            variables = category.toAniListVariables(page, perPage)
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun fetchMangaList(
        category: MangaCategory,
        page: Int,
        perPage: Int = 20,
    ): AniListResponse<AniListPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.FETCH_MANGA_LIST,
            variables = GraphQLRequest.Variables(
                perPage = perPage,
                page = page,
                sort = category.toAniListSort()
            )
        )
        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }


    suspend fun fetchAnimeHeroList(limit: Int): AniListResponse<AniListPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.FETCH_ANIME_HERO_LIST,
            variables = GraphQLRequest.Variables(
                perPage = limit,
                page = 1,
                // 1. Sort by what's hot right now
                sort = listOf("TRENDING_DESC"),
                // 2. Only show things currently airing (The "Seasonal Spotlight")
                status = "RELEASING",
                // 3. CRITICAL: Prevent NSFW content from appearing on the huge banner
                isAdult = false
            )
        )
        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun fetchMangaHeroList(limit: Int): AniListResponse<AniListPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.FETCH_MANGA_HERO_LIST,
            variables = GraphQLRequest.Variables(
                perPage = limit,
                page = 1,
                // 1. Sort by trending
                sort = listOf("TRENDING_DESC"),
                // 2. Only show manga that are actively publishing new chapters
                status = "RELEASING",
                // 3. CRITICAL: Prevent NSFW content
                isAdult = false
            )
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }


    suspend fun searchAnime(
        variables: GraphQLRequest.Variables,
    ): AniListResponse<AniListPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.SEARCH_ANIME,
            variables = variables
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun searchManga(
        variables: GraphQLRequest.Variables,
    ): AniListResponse<AniListPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.SEARCH_MANGA,
            variables = variables
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun getAnimeDetails(id: Int): AniListResponse<AniListSingleMediaData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.GET_ANIME_DETAILS,
            variables = GraphQLRequest.Variables(id = id)
        )
        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun getMangaDetails(id: Int): AniListResponse<AniListSingleMediaData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.GET_MANGA_DETAILS,
            variables = GraphQLRequest.Variables(id = id)
        )
        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun getMediaRecommendations(
        id: Int,
        page: Int,
    ): AniListResponse<AniListRecommendationsResponse> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.GET_MEDIA_RECOMMENDATIONS,
            variables = GraphQLRequest.Variables(id = id, page = page)
        )
        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun getMediaReviews(id: Int, page: Int): AniListResponse<AniListReviewsResponse> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.GET_MEDIA_REVIEWS,
            variables = GraphQLRequest.Variables(id = id, page = page)
        )
        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

// ========== USER LIST FUNCTIONS ==========

    /**
     * Get user's anime list by status
     * @param userId The user's AniList ID
     * @param status MediaListStatus (CURRENT, COMPLETED, PLANNING, DROPPED, PAUSED, REPEATING)
     * @param page Page number for pagination
     * @param perPage Number of items per page (default 20)
     * @param token Optional auth token for private lists
     */
    suspend fun getUserAnimeList(
        userId: Int,
        status: String,
        page: Int = 1,
        perPage: Int = 20,
    ): AniListResponse<AniListUserPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.GET_USER_ANIME_LIST,
            variables = GraphQLRequest.Variables(
                userId = userId,
                status = status,
                page = page,
                perPage = perPage
            )
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    /**
     * Get user's manga list by status
     * @param userId The user's AniList ID
     * @param status MediaListStatus (CURRENT, COMPLETED, PLANNING, DROPPED, PAUSED, REPEATING)
     * @param page Page number for pagination
     * @param perPage Number of items per page (default 20)
     * @param token Optional auth token for private lists
     */
    suspend fun getUserMangaList(
        userId: Int,
        status: String,
        page: Int = 1,
        perPage: Int = 20,
    ): AniListResponse<AniListUserPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.GET_USER_MANGA_LIST,
            variables = GraphQLRequest.Variables(
                userId = userId,
                status = status,
                page = page,
                perPage = perPage
            )
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    /**
     * Search within user's anime/manga list
     * @param userId The user's AniList ID
     * @param type MediaType (ANIME or MANGA)
     * @param search Search query string
     * @param page Page number for pagination
     * @param perPage Number of items per page (default 20)
     * @param token Optional auth token for private lists
     */
    suspend fun searchUserMediaList(
        userId: Int,
        type: String,
        search: String,
        page: Int = 1,
        perPage: Int = 20,
    ): AniListResponse<AniListPageData> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.SEARCH_USER_LIST,
            variables = GraphQLRequest.Variables(
                userId = userId,
                type = type,
                search = search,
                page = page,
                perPage = perPage
            )
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }


// ========== MEDIA LIST MUTATIONS ==========

    /**
     * Update or create a media list entry
     * @param mediaId The ID of the anime/manga
     * @param progress Current episode/chapter number
     * @param status MediaListStatus (CURRENT, COMPLETED, PLANNING, DROPPED, PAUSED, REPEATING)
     * @param score Score value (0-100)
     * @param token Auth token (required)
     */
    suspend fun updateMediaListEntry(
        mediaId: Int,
        progress: Int? = null,
        status: String? = null,
        score: Float? = null,

        ): AniListResponse<AniListUpdateData> { // Fixed Return Type
        val requestBody = GraphQLRequest(
            query = AniListQueries.UPDATE_ENTRY,
            variables = GraphQLRequest.Variables(
                mediaId = mediaId,
                progress = progress,
                status = status,
                score = score,
            )
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }


    /**
     * Delete a media list entry
     * @param entryId The ID of the list entry
     * @param token Auth token (required)
     */
    suspend fun deleteMediaListEntry(
        entryId: Int,
    ): AniListResponse<AniListDeleteData> { // Fixed Return Type
        val requestBody = GraphQLRequest(
            query = AniListQueries.DELETE_ENTRY,
            variables = GraphQLRequest.Variables(
                id = entryId
            )
        )

        return client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }


    suspend fun getCurrentUser(): AniListResponse<ViewerWrapper> {
        val requestBody = GraphQLRequest(
            query = AniListQueries.GET_USER_VIEWER,
            variables = GraphQLRequest.Variables()
        )


        val response = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        return response.body()
    }
}