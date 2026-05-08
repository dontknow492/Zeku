package com.ghost.zeku.data.remote.mal


import com.ghost.zeku.data.remote.mal.extra.getCurrentSeasonAndYear
import com.ghost.zeku.data.remote.mal.model.MalListStatus
import com.ghost.zeku.data.remote.mal.model.MalMediaDto
import com.ghost.zeku.data.remote.mal.model.MalPagedResponse
import com.ghost.zeku.data.remote.mal.model.MalUserDto
import com.ghost.zeku.domain.model.enum.MediaCategory
import com.ghost.zeku.domain.model.enum.MediaType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import zeku.composeApp.BuildConfig

class MalApi(
    private val client: HttpClient
) {

    private val baseUrl = BuildConfig.MAL_BASE_URL

    // =============================================================================================
    // HELPERS
    // =============================================================================================

    private fun calculateOffset(
        page: Int,
        limit: Int
    ): Int {
        return if (page <= 1) 0 else (page - 1) * limit
    }

    private fun mediaPath(mediaType: MediaType): String {
        return when (mediaType) {
            MediaType.ANIME -> "anime"
            MediaType.MANGA -> "manga"
            else -> error("Unsupported media type: $mediaType")
        }
    }

    private fun defaultFields(mediaType: MediaType): String {
        return when (mediaType) {
            MediaType.ANIME -> MalApiConstants.ANIME_FIELDS
            MediaType.MANGA -> MalApiConstants.MANGA_FIELDS
            else -> MalApiConstants.COMMON_FIELDS
        }
    }

    private fun detailFields(mediaType: MediaType): String {
        return when (mediaType) {
            MediaType.ANIME -> MalApiConstants.ANIME_DETAILS_FIELDS
            MediaType.MANGA -> MalApiConstants.MANGA_DETAILS_FIELDS
            else -> MalApiConstants.COMMON_FIELDS
        }
    }

    private suspend inline fun <reified T> get(
        path: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return client.get("$baseUrl/$path", block).body()
    }

    private suspend inline fun <reified T> put(
        path: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return client.put("$baseUrl/$path", block).body()
    }

    private suspend inline fun <reified T> patch(
        path: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return client.patch("$baseUrl/$path", block).body()
    }

    // =============================================================================================
    // DISCOVERY
    // =============================================================================================

    suspend fun fetchMediaList(
        mediaType: MediaType,
        category: MediaCategory,
        page: Int,
        limit: Int = 20,
    ): MalPagedResponse<MalMediaDto> {

        val path = mediaPath(mediaType)

        // =========================================
        // SEASONAL ANIME
        // =========================================

        if (
            mediaType == MediaType.ANIME &&
            category == MediaCategory.SEASONAL
        ) {
            val (season, year) = getCurrentSeasonAndYear()

            return get("$path/season/$year/${season.lowercase()}") {

                parameter("limit", limit)

                parameter(
                    "offset",
                    calculateOffset(page, limit)
                )

                parameter(
                    "fields",
                    detailFields(mediaType)
                )

                parameter("sort", "anime_score")
            }
        }

        // =========================================
        // RANKING
        // =========================================

        val rankingType = category.toMalRankingType(mediaType)
            ?: error("Category $category not supported for $mediaType")

        return get("$path/ranking") {

            parameter("ranking_type", rankingType)

            parameter("limit", limit)

            parameter(
                "offset",
                calculateOffset(page, limit)
            )

            parameter(
                "fields",
                defaultFields(mediaType)
            )
        }
    }

    // =============================================================================================
    // HERO
    // =============================================================================================

    suspend fun fetchHeroList(
        mediaType: MediaType,
        limit: Int = 10
    ): MalPagedResponse<MalMediaDto> {

        val path = mediaPath(mediaType)

        val rankingType = when (mediaType) {
            MediaType.ANIME -> "airing"
            MediaType.MANGA -> "publishing"
            else -> "all"
        }

        return get("$path/ranking") {

            parameter("ranking_type", rankingType)

            parameter("limit", limit)

            parameter("offset", 0)

            parameter(
                "fields",
                defaultFields(mediaType)
            )
        }
    }

    // =============================================================================================
    // SEARCH
    // =============================================================================================

    suspend fun searchMedia(
        mediaType: MediaType,
        query: String?,
        page: Int,
        limit: Int = 20,

        status: String? = null,

        genres: String? = null,
    ): MalPagedResponse<MalMediaDto> {

        val path = mediaPath(mediaType)

        return get(path) {

            if (!query.isNullOrBlank()) {
                parameter("q", query)
            }

            parameter("limit", limit)

            parameter(
                "offset",
                calculateOffset(page, limit)
            )

            status?.let {
                parameter("status", it)
            }

            genres?.let {
                parameter("genres", it)
            }

            parameter(
                "fields",
                defaultFields(mediaType)
            )
        }
    }

    // =============================================================================================
    // DETAILS
    // =============================================================================================

    suspend fun getMediaDetails(
        mediaType: MediaType,
        id: Int
    ): MalMediaDto {

        val path = mediaPath(mediaType)

        return get("$path/$id") {

            parameter(
                "fields",
                detailFields(mediaType)
            )
        }
    }

    // =============================================================================================
    // USER LIST
    // =============================================================================================

    suspend fun getUserLibrary(
        mediaType: MediaType,
        username: String = "@me",

        status: String? = null,

        page: Int = 1,

        limit: Int = 20,
    ): MalPagedResponse<MalMediaDto> {

        val path = mediaPath(mediaType)

        return get("users/$username/${path}list") {

            status?.let {
                parameter("status", it)
            }

            parameter("limit", limit)

            parameter(
                "offset",
                calculateOffset(page, limit)
            )

            parameter(
                "fields",
                "list_status,${defaultFields(mediaType)}"
            )
        }
    }

    // =============================================================================================
    // TRACKING
    // =============================================================================================

    suspend fun updateListEntry(
        mediaType: MediaType,
        mediaId: Int,

        status: String? = null,

        progress: Int? = null,

        volumes: Int? = null,

        score: Int? = null,
    ): MalListStatus {

        val path = mediaPath(mediaType)

        return put("$path/$mediaId/my_list_status") {

            setBody(
                FormDataContent(
                    Parameters.build {

                        status?.let {
                            append("status", it)
                        }

                        progress?.let {
                            append(
                                when (mediaType) {
                                    MediaType.ANIME ->
                                        "num_watched_episodes"

                                    MediaType.MANGA ->
                                        "num_chapters_read"

                                    else ->
                                        "progress"
                                },
                                it.toString()
                            )
                        }

                        if (mediaType == MediaType.MANGA) {
                            volumes?.let {
                                append(
                                    "num_volumes_read",
                                    it.toString()
                                )
                            }
                        }

                        score?.let {
                            append("score", it.toString())
                        }
                    }
                )
            )
        }
    }

    suspend fun deleteListEntry(
        mediaType: MediaType,
        mediaId: Int
    ): Boolean {

        val path = mediaPath(mediaType)

        val response = client.delete(
            "$baseUrl/$path/$mediaId/my_list_status"
        )

        return response.status.isSuccess()
    }

    // =============================================================================================
    // USER
    // =============================================================================================

    suspend fun getCurrentUser(): MalUserDto {

        return get("users/@me") {

            parameter(
                "fields",
                "id,name,picture,anime_statistics"
            )
        }
    }
}
