package com.ghost.zeku.data.remote.jikan


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class JikanApi(private val client: HttpClient) {
    private val baseUrl = "https://api.jikan.moe/v4"

    // --- Characters (Eager Load) ---

    suspend fun getAnimeCharacters(id: Int): JikanResponse<List<JikanCharacterEdge>> {
        return client.get("$baseUrl/anime/$id/characters").body()
    }

    suspend fun getMangaCharacters(id: Int): JikanResponse<List<JikanCharacterEdge>> {
        return client.get("$baseUrl/manga/$id/characters").body()
    }

    // --- Lazy Loads (Paginated) ---

    suspend fun getAnimeEpisodes(id: Int, page: Int): JikanResponse<List<JikanEpisode>> {
        return client.get("$baseUrl/anime/$id/episodes") {
            parameter("page", page)
        }.body()
    }

    suspend fun getAnimeReviews(id: Int, page: Int): JikanResponse<List<JikanReview>> {
        return client.get("$baseUrl/anime/$id/reviews") {
            parameter("page", page)
            parameter("spoilers", true) // Ensure spoilers are included so we can blur them
        }.body()
    }

    suspend fun getMangaReviews(id: Int, page: Int): JikanResponse<List<JikanReview>> {
        return client.get("$baseUrl/manga/$id/reviews") {
            parameter("page", page)
            parameter("spoilers", true)
        }.body()
    }

    // Note: Jikan's recommendations endpoint is NOT paginated natively, it returns all of them.
    suspend fun getAnimeRecommendations(id: Int): JikanResponse<List<JikanRecommendationEdge>> {
        return client.get("$baseUrl/anime/$id/recommendations").body()
    }

    suspend fun getMangaRecommendations(id: Int): JikanResponse<List<JikanRecommendationEdge>> {
        return client.get("$baseUrl/manga/$id/recommendations").body()
    }
}