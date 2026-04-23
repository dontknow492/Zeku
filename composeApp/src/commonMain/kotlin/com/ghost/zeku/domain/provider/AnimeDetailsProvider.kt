package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.*

interface AnimeDetailsProvider {
    /**
     * EAGER: Fetches the core details, characters, links, and trailer.
     * Usually executed as soon as the user clicks the Anime card.
     */
    suspend fun getAnimeDetails(id: Int): ApiResult<AnimeDetails>

    /**
     * LAZY: Fetches episodes.
     * Separated so we can paginate a 1000-episode anime like One Piece without freezing the app.
     * Can also be routed to a DIFFERENT provider (e.g., a streaming scraper) later!
     */
    suspend fun getAnimeEpisodes(id: Int, page: Int, perPage: Int = 50): ApiResult<PageResult<Episode>>

    /**
     * LAZY: Fetches recommendations.
     * We return 'Anime' models so you can reuse your existing Anime Card UI!
     */
    suspend fun getAnimeRecommendations(id: Int, page: Int): ApiResult<PageResult<Anime>>

    /**
     * LAZY (ONLINE-ONLY): Fetches reviews or comments.
     * This is paginated because popular media can have thousands of text-heavy reviews.
     * Best handled by GenericPagingSource rather than Room to prevent database bloat.
     */
    suspend fun getAnimeReviews(id: Int, page: Int, perPage: Int = 20): ApiResult<PageResult<Review>>

}
