package com.ghost.zeku.data.remote.jikan

import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.enum.CharacterRole
import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.Episode
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.MediaCharacter
import com.ghost.zeku.domain.model.media.Review

// --- Jikan Mappers ---

fun JikanCharacterEdge.toDomain(): MediaCharacter? {
    val charNode = this.character ?: return null
    return MediaCharacter(
        id = charNode.mal_id ?: return null,
        name = charNode.name ?: "Unknown",
        imageUrl = charNode.images?.jpg?.image_url,
        role = CharacterRole.fromString(this.role) // Uses the safe enum fallback
    )
}

fun JikanEpisode.toDomain(): Episode {
    return Episode(
        id = this.mal_id?.toString() ?: "",
        number = this.mal_id ?: 0,
        title = this.title,
        description = this.synopsis,
        thumbnail = null, // Jikan rarely provides thumbnails for episodes
        isFiller = this.filler
    )
}

fun JikanReview.toDomain(): Review {
    return Review(
        id = this.mal_id ?: 0,
        author = this.user?.username ?: "Anonymous",
        authorAvatar = this.user?.images?.jpg?.image_url,
        score = this.score?.let { it * 10 }, // Convert 10-point to 100-point scale
        summary = null, // Jikan doesn't provide a short summary
        body = this.review ?: "",
        upvotes = 0,
        isSpoiler = this.is_spoiler || this.tags?.contains("Spoiler") == true,
        createdAt = null // Date parsing can be added later if needed
    )
}


fun JikanRecommendationEdge.toMediaDomain(mediaType: MediaType): Media? {
    val entry = this.entry ?: return null
    return Media(
        id = entry.mal_id ?: return null,
        source = ProviderType.MYANIMELIST,
        title = MediaTitle(romaji = entry.title, english = null, native = null),
        coverImage = entry.images?.jpg?.large_image_url ?: entry.images?.jpg?.image_url ?: "",
        format = MediaFormat.UNKNOWN, // Jikan doesn't return format in the basic recommendation node
        genres = emptyList(),
        score = null,
        mediaType = mediaType
    )
}
