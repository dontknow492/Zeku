package com.ghost.zeku.domain.model.media

import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.ProviderType
import kotlinx.serialization.Serializable

interface Media {
    val id: Int
    val source: ProviderType

    val format: MediaFormat

    val title: MediaTitle

    val coverImage: String
    val bannerImage: String?

    val description: String?
    val genres: List<String>

    val status: MediaReleaseStatus?

    val score: Float?

    val startDate: MediaDate?

    val trackEntry: TrackEntry?
}

@Serializable
data class Manga(
    override val id: Int,
    override val source: ProviderType,

    override val title: MediaTitle,

    override val format: MediaFormat = MediaFormat.UNKNOWN,

    override val coverImage: String,
    override val bannerImage: String? = null,

    override val description: String? = null,
    override val genres: List<String> = emptyList(),

    override val status: MediaReleaseStatus? = null,

    override val score: Float? = null,

    override val startDate: MediaDate? = null,

    // Manga-specific
    val chapters: Int? = null,
    val volumes: Int? = null,

    // optional extras
    val author: String? = null,

    override val trackEntry: TrackEntry? = null
) : Media

@Serializable
data class Anime(
    override val id: Int,
    override val source: ProviderType,

    override val title: MediaTitle,

    override val format: MediaFormat = MediaFormat.UNKNOWN,

    override val coverImage: String,
    override val bannerImage: String? = null,

    override val description: String? = null,
    override val genres: List<String> = emptyList(),

    override val status: MediaReleaseStatus? = null,

    override val score: Float? = null,

    override val startDate: MediaDate? = null,

    // Anime-specific
    val episodes: Int? = null,
    val duration: Int? = null, // per episode (minutes)
    val studio: String? = null,

    override val trackEntry: TrackEntry? = null
) : Media