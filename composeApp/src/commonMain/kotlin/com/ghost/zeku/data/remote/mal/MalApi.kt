package com.ghost.zeku.data.remote.mal

import com.ghost.zeku.data.remote.mal.extra.getCurrentSeasonAndYear
import com.ghost.zeku.data.remote.mal.model.*
import com.ghost.zeku.data.remote.mal.providers.toMalRankingType
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.MangaCategory
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import zeku.composeApp.BuildConfig

class MalApi(private val client: HttpClient) {
    private val baseUrl = "https://api.myanimelist.net/v2"

    // Helper function to add Auth header
    private fun HttpRequestBuilder.addAuthHeader(token: String?) {
        token?.let {
            header(HttpHeaders.Authorization, "Bearer $it")
            header("X-MAL-CLIENT-ID", BuildConfig.MAL_CLIENT_ID)
        }
    }

    // Helper to calculate offset from page number
    private fun calculateOffset(page: Int, limit: Int): Int {
        return if (page > 0) (page - 1) * limit else 0
    }

    // ========== GENERAL MEDIA ==========

    suspend fun fetchAnimeList(
        category: AnimeCategory,
        page: Int,
        limit: Int = 20,
        token: String? = null
    ): MalPagedResponse<MalAnimeDto> {
        val offset = calculateOffset(page, limit)

        return if (category == AnimeCategory.SEASONAL) {
            // 1. Handle Seasonal Anime (Custom Endpoint)
            val (season, year) = getCurrentSeasonAndYear()
            client.get("$baseUrl/anime/season/$year/${season.lowercase()}") {
                addAuthHeader(token)
                parameter("limit", limit)
                parameter("offset", offset)
                parameter("fields", MalApiConstants.ANIME_FIELDS)
                parameter("sort", "anime_score") // MAL default for seasonal
            }.body()
        } else {
            // 2. Handle Ranking categories
            val rankingType = category.toMalRankingType()
                ?: throw IllegalArgumentException("Category ${category.name} not supported by MAL.")

            client.get("$baseUrl/anime/ranking") {
                addAuthHeader(token)
                parameter("ranking_type", rankingType)
                parameter("limit", limit)
                parameter("offset", offset)
                parameter("fields", MalApiConstants.ANIME_FIELDS)
            }.body()
        }
    }

    suspend fun fetchMangaList(
        category: MangaCategory,
        page: Int,
        limit: Int = 20,
        token: String? = null
    ): MalPagedResponse<MalMangaDto> {
        // 1. Get the MAL-specific ranking type
        val rankingType = category.toMalRankingType()
            ?: throw IllegalArgumentException("Category ${category.name} is not supported by MAL ranking API.")

        // 2. Execute the request with centralized constants
        return client.get("$baseUrl/manga/ranking") {
            addAuthHeader(token)
            parameter("ranking_type", rankingType)
            parameter("limit", limit)
            parameter("offset", calculateOffset(page, limit))
            parameter("fields", MalApiConstants.MANGA_FIELDS) // Uses our centralized field list
        }.body()
    }

    suspend fun searchAnime(
        query: String,
        page: Int,
        limit: Int = 20,
        status: String? = null,
        genres: String? = null,
        token: String? = null
    ): MalPagedResponse<MalAnimeDto> {
        return client.get("$baseUrl/anime") {
            addAuthHeader(token)
            // MAL requires a 'q' parameter, but it fails if empty.
            // Some endpoints allow empty 'q' if genres are provided.
            if (query.isNotBlank()) {
                parameter("q", query)
            }

            parameter("limit", limit)
            parameter("offset", calculateOffset(page, limit))

            // Only append these if they aren't null
            status?.let { parameter("status", it) }
            genres?.let { parameter("genres", it) }

            parameter("fields", MalApiConstants.ANIME_FIELDS)
        }.body()
    }

    suspend fun searchManga(
        query: String,
        page: Int,
        limit: Int = 20,
        status: String? = null,
        genres: String? = null,
        token: String? = null
    ): MalPagedResponse<MalMangaDto> {
        return client.get("$baseUrl/manga") {
            addAuthHeader(token)
            if (query.isNotBlank()) {
                parameter("q", query)
            }

            parameter("limit", limit)
            parameter("offset", calculateOffset(page, limit))

            status?.let { parameter("status", it) }
            genres?.let { parameter("genres", it) }

            parameter("fields", MalApiConstants.MANGA_FIELDS)
        }.body()
    }


    suspend fun getAnimeDetails(id: Int, token: String? = null): MalAnimeDto {
        return client.get("$baseUrl/anime/$id") {
            addAuthHeader(token)
            parameter("fields", MalApiConstants.ANIME_DETAILS_FIELDS)
        }.body()
    }

    suspend fun getMangaDetails(id: Int, token: String? = null): MalMangaDto {
        return client.get("$baseUrl/manga/$id") {
            addAuthHeader(token)
            parameter("fields", MalApiConstants.MANGA_DETAILS_FIELDS)
        }.body()
    }

    // ========== USER LIST FUNCTIONS ==========

    suspend fun getUserAnimeList(
        username: String = "@me",
        status: String? = null,
        page: Int = 1,
        limit: Int = 20,
        token: String? = null
    ): MalPagedResponse<MalAnimeDto> {
        return client.get("$baseUrl/users/$username/animelist") {
            addAuthHeader(token)
            status?.let { parameter("status", it) }
            parameter("limit", limit)
            parameter("offset", calculateOffset(page, limit))
            // Prepend list_status so it returns user progress
            parameter("fields", "list_status,${MalApiConstants.ANIME_FIELDS}")
        }.body()
    }

    suspend fun getUserMangaList(
        username: String = "@me",
        status: String? = null,
        page: Int = 1,
        limit: Int = 20,
        token: String? = null
    ): MalPagedResponse<MalMangaDto> {
        Napier.d("Get user manga list")
        val response = client.get("$baseUrl/users/$username/mangalist") {
            addAuthHeader(token)
            status?.let { parameter("status", it) }
            parameter("limit", limit)
            parameter("offset", calculateOffset(page, limit))
            parameter("fields", "list_status,${MalApiConstants.MANGA_FIELDS}")
        }
        return response.body()
    }

    // ========== MEDIA LIST MUTATIONS (Requires Auth) ==========

    suspend fun updateAnimeListEntry(
        mediaId: Int,
        status: String? = null,
        progress: Int? = null,
        score: Int? = null,
        token: String
    ): MalListStatus {
        return client.put("$baseUrl/anime/$mediaId/my_list_status") {
            addAuthHeader(token)
            setBody(FormDataContent(Parameters.build {
                status?.let { append("status", it) }
                progress?.let { append("num_watched_episodes", it.toString()) }
                score?.let { append("score", it.toString()) }
            }))
        }.body()
    }

    suspend fun updateMangaListEntry(
        mediaId: Int,
        status: String? = null,
        progress: Int? = null,
        volumes: Int? = null,
        score: Int? = null,
        token: String
    ): MalListStatus {
        return client.put("$baseUrl/manga/$mediaId/my_list_status") {
            addAuthHeader(token)
            setBody(FormDataContent(Parameters.build {
                status?.let { append("status", it) }
                progress?.let { append("num_chapters_read", it.toString()) }
                volumes?.let { append("num_volumes_read", it.toString()) }
                score?.let { append("score", it.toString()) }
            }))
        }.body()
    }

    suspend fun deleteAnimeListEntry(mediaId: Int, token: String) {
        client.delete("$baseUrl/anime/$mediaId/my_list_status") {
            addAuthHeader(token)
        }
    }

    suspend fun deleteMangaListEntry(mediaId: Int, token: String) {
        client.delete("$baseUrl/manga/$mediaId/my_list_status") {
            addAuthHeader(token)
        }
    }

    // ========== USER PROFILE ==========

    suspend fun getCurrentUser(token: String): MalUserDto {
        return client.get("$baseUrl/users/@me") {
            addAuthHeader(token)
            parameter("fields", "id,name,picture,anime_statistics")
        }.body()
    }
}
