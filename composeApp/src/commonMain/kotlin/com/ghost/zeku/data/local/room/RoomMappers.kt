package com.ghost.zeku.data.local.room


import com.ghost.zeku.data.local.room.entities.*
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.media.MediaFormat
import com.ghost.zeku.domain.model.media.Chapter
import com.ghost.zeku.domain.model.media.Episode
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.MediaDetails

/**
 * Converts a Room AnimeEntity to the Anime Domain Model.
 */

fun MediaEntity.toDomain(): Media {
    return Media(
        id = this.id,
        source = this.provider,
        mediaType = this.mediaType,

        // Safely parse the format String into the Enum. Fallback to UNKNOWN if it fails.
        format = try {
            if (!this.format.isNullOrBlank()) {
                MediaFormat.valueOf(this.format.uppercase())
            } else {
                MediaFormat.UNKNOWN
            }
        } catch (e: IllegalArgumentException) {
            MediaFormat.UNKNOWN
        },

        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        status = this.status,
        score = this.score,
        startDate = this.startDate,
        tags = this.tags,
        popularity = this.popularity,

        // These fields are in Media but not in MediaEntity.
        // We set them to null as they aren't cached locally.
        favourites = null,
        rank = null,

        episodes = this.episodes,
        duration = this.duration,
        studio = this.studio,

        chapters = this.chapters,
        volumes = this.volumes,
        author = this.author
    )
}

fun Media.toEntity(): MediaEntity {
    return MediaEntity(
        id = this.id,
        provider = this.source,
        mediaType = this.mediaType,
        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        tags = this.tags,
        status = this.status,
        score = this.score,
        popularity = this.popularity,
        startDate = this.startDate,

        // Convert Enum back to String for the database
        format = this.format.name,

        // These fields exist in MediaEntity but not in the Media domain model.
        // We set them to null.
        endDate = null,
        season = null,
        seasonYear = null,
        countryOfOrigin = null,
        sourceMaterial = null,
        siteUrl = null,
        nextEpisodeAt = null,

        episodes = this.episodes,
        duration = this.duration,
        studio = this.studio,

        chapters = this.chapters,
        volumes = this.volumes,
        author = this.author

        // Note: createdAt and updatedAt are omitted here so they automatically
        // use the default System.currentTimeMillis() defined in the Entity constructor.
    )
}


fun MediaDetailsEntity.toDomain(): MediaDetails {
    return this.details
}

fun MediaDetails.toEntity(): MediaDetailsEntity {
    return MediaDetailsEntity(
        id = this.id,
        provider = this.source,
        mediaType = this.mediaType,
        details = this,
    )
}


fun MediaDetails.toBaseEntity(): MediaEntity {
    return MediaEntity(
        id = this.id,
        provider = this.source,
        mediaType = this.mediaType,
        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        tags = this.tags.map { it.name }, // Extracting just the string names from MediaTag
        status = this.status,

        // Use averageScore as the primary score. Fallback to meanScore.
        // Convert Double to Float as required by MediaEntity.
        score = this.averageScore?.toFloat() ?: this.meanScore?.toFloat(),
        popularity = this.popularity,
        startDate = this.startDate,
        endDate = this.endDate,

        // Convert Enum to String. Fallback to UNKNOWN if null.
        format = this.format?.name ?: MediaFormat.UNKNOWN.name,

        season = this.season?.name,
        seasonYear = this.seasonYear,
        countryOfOrigin = this.countryOfOrigin,
        sourceMaterial = this.sourceMaterial,

        // We take the first external link URL if it exists
        siteUrl = this.externalLinks.firstOrNull()?.url,

        episodes = this.totalEpisodes,
        duration = this.durationPerEpisode,

        // We take the first studio name if it exists (usually the main animation studio)
        studio = this.studios.firstOrNull()?.name,

        // Extract the airing time timestamp if it exists
        nextEpisodeAt = this.nextAiringEpisode?.timeUntilAiring,

        chapters = this.totalChapters,
        volumes = this.totalVolumes,

        // We take the first author's name if it exists
        author = this.authors.firstOrNull()?.name
    )
}


fun EpisodeEntity.toDomain(): Episode {
    return Episode(
        id = this.id,
        number = this.number.toInt(), // Domain model uses Int, Entity uses Float to support x.5 episodes safely
        title = this.title,
        description = this.description,
        thumbnail = this.thumbnail,
        isFiller = this.isFiller
    )
}

fun ChapterEntity.toDomain(): Chapter {
    return Chapter(
        id = this.id,
        number = this.number,
        title = this.title,
        volume = this.volume
    )
}


// Mapper functions to keep Domain and Data layers separate
fun UserEntity.toDomain() = UserProfile(
    id = userId,
    username = username,
    avatarUrl = avatarUrl,
    bannerUrl = bannerUrl,
    source = this.providerType
)

fun UserProfile.toEntity() = UserEntity(
    providerType = source,
    userId = id,
    username = username,
    avatarUrl = avatarUrl,
    bannerUrl = bannerUrl
)