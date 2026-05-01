package com.ghost.zeku.data.remote.jikan


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import zeku.composeApp.BuildConfig

class JikanApi(private val client: HttpClient) {
    private val baseUrl = BuildConfig.JIKAN_BASE_URL

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

    // Note: Make sure you have the ContentNegotiation plugin installed in your HttpClient
    suspend fun getAnimeDetail(id: Int): JikanResponse<JikanAnime> {
        return client.get("${baseUrl}/anime/$id") {
            // Jikan supports additional fields like characters or recommendations
            // if you add them to the query, but basic detail is standard.
        }.body()
    }

    suspend fun getMangaDetail(id: Int): JikanResponse<JikanManga> {
        return client.get("$baseUrl/manga/$id") {
            // You can add parameters here if needed
        }.body()
    }

}