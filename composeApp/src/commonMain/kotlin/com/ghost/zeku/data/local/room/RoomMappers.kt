package com.ghost.zeku.data.local.room


import com.ghost.zeku.data.local.room.entities.*
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.*

/**
 * Converts a Room AnimeEntity to the Anime Domain Model.
 */
fun AnimeEntity.toDomain(): Anime {
    return Anime(
        id = this.id,
        source = this.source,
        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        status = this.status,
        score = this.score,
        startDate = this.startDate,
        episodes = this.episodes,
        duration = this.duration,
        studio = this.studio,
        trackEntry = this.trackEntry
    )
}

/**
 * Converts an Anime Domain Model to the Room AnimeEntity.
 */
fun Anime.toEntity(): AnimeEntity {
    return AnimeEntity(
        id = this.id,
        source = this.source,
        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        status = this.status,
        score = this.score,
        startDate = this.startDate,
        episodes = this.episodes,
        duration = this.duration,
        studio = this.studio,
        trackEntry = this.trackEntry
    )
}

/**
 * Converts a Room MangaEntity to the Manga Domain Model.
 */
fun MangaEntity.toDomain(): Manga {
    return Manga(
        id = this.id,
        source = this.source,
        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        status = this.status,
        score = this.score,
        startDate = this.startDate,
        chapters = this.chapters,
        volumes = this.volumes,
        author = this.author,
        trackEntry = this.trackEntry
    )
}

/**
 * Converts a Manga Domain Model to the Room MangaEntity.
 */
fun Manga.toEntity(): MangaEntity {
    return MangaEntity(
        id = this.id,
        source = this.source,
        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        status = this.status,
        score = this.score,
        startDate = this.startDate,
        chapters = this.chapters,
        volumes = this.volumes,
        author = this.author,
        trackEntry = this.trackEntry
    )
}


fun AnimeDetailsEntity.toDomain(): AnimeDetails {
    // Since we used the JSON converter, the details object is completely intact!
    return this.details
}

fun AnimeDetails.toEntity(): AnimeDetailsEntity {
    return AnimeDetailsEntity(
        id = this.id,
        source = this.source,
        details = this, // The Room JSON converter handles this automatically
        updatedAt = System.currentTimeMillis()
    )
}

fun MangaDetailsEntity.toDomain(): MangaDetails {
    return this.details
}

fun MangaDetails.toEntity(): MangaDetailsEntity {
    return MangaDetailsEntity(
        id = this.id,
        source = this.source,
        details = this,
        updatedAt = System.currentTimeMillis()
    )
}


// ========================================================================
// DETAILS -> BASE_ENTITY (To keep Home Screen lists fresh)
// ========================================================================

/**
 * Converts the rich Details object back into a lightweight List Entity.
 * Use this to update the Home Screen cache after fetching details!
 */
fun AnimeDetails.toBaseEntity(): AnimeEntity {
    return AnimeEntity(
        id = this.id,
        source = this.source,
        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        status = MediaReleaseStatus.fromString(this.status),
        score = this.averageScore?.toFloat(),

        // These fields aren't in the Details view right now, so they default to null.
        // Room will just ignore them or overwrite them with null, which is perfectly safe.
        startDate = null,
        duration = null,
        studio = null,

        trackEntry = this.trackEntry,
        episodes = this.totalEpisodes,

        updatedAt = System.currentTimeMillis()
    )
}

fun MangaDetails.toBaseEntity(): MangaEntity {
    return MangaEntity(
        id = this.id,
        source = this.source,
        title = this.title,
        coverImage = this.coverImage,
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres,
        status = MediaReleaseStatus.fromString(this.status),
        score = this.averageScore?.toFloat(),
        startDate = null,
        author = null,

        trackEntry = this.trackEntry,
        chapters = this.totalChapters,
        volumes = this.totalVolumes,

        updatedAt = System.currentTimeMillis()
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