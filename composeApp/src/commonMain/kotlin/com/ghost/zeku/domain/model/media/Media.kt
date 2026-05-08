package com.ghost.zeku.domain.model.media

import androidx.compose.ui.util.fastAny
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import kotlinx.serialization.Serializable


@Serializable
data class Media(

    // ------------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------------

    val id: Int,

    val source: ProviderType,

    val mediaType: MediaType,

    // ------------------------------------------------------------------------
    // Core Metadata
    // ------------------------------------------------------------------------

    val format: MediaFormat = MediaFormat.UNKNOWN,

    val title: MediaTitle,

    val coverImage: String,

    val bannerImage: String? = null,

    val description: String? = null,

    val genres: List<String> = emptyList(),

    val status: MediaReleaseStatus? = null,

    val score: Float? = null,

    val startDate: MediaDate? = null,

    // ------------------------------------------------------------------------
    // Shared Extended Metadata
    // ------------------------------------------------------------------------

    val tags: List<String> = emptyList(),

    val popularity: Int? = null,

    val favourites: Int? = null,

    val rank: Int? = null,

    // ------------------------------------------------------------------------
    // Anime Fields
    // ------------------------------------------------------------------------

    val episodes: Int? = null,

    val duration: Int? = null,

    val studio: String? = null,

    // ------------------------------------------------------------------------
    // Manga / Novel Fields
    // ------------------------------------------------------------------------

    val chapters: Int? = null,

    val volumes: Int? = null,

    val author: String? = null
) {
    fun isAdult(): Boolean {
        val adultGenre = listOf<String>("Hentai", "Adult", "Mature", "Gore")
        return genres.fastAny { adultGenre.contains(it) } || title.getDisplayTitle().contains("(18+)")
    }
}
