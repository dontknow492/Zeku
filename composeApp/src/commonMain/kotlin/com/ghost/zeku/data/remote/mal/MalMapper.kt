package com.ghost.zeku.data.remote.mal

import com.ghost.zeku.data.remote.jikan.JikanCharacterEdge
import com.ghost.zeku.data.remote.jikan.toDomain
import com.ghost.zeku.data.remote.mal.model.*
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.*
import com.ghost.zeku.domain.model.media.*

// ==========================================
// 1. ANIME MAPPERS
// ==========================================

fun MalNode<MalAnimeDto>.toAnimeDomain(): Anime? {
    val dto = this.node ?: return null

    return Anime(
        id = dto.id ?: 0,
        source = ProviderType.MYANIMELIST,
        title = MediaTitle(
            romaji = dto.title ?: "Unknown",
            english = dto.alternativeTitles?.en,
            native = dto.alternativeTitles?.ja
        ),
        coverImage = dto.mainPicture?.large ?: dto.mainPicture?.medium ?: "",
        bannerImage = null, // MAL doesn't natively support banners
        description = dto.synopsis,
        genres = dto.genres?.mapNotNull { it.name } ?: emptyList(),
        status = dto.status.toMediaReleaseStatus(),
        score = dto.mean?.toFloat(),
        startDate = dto.startDate.toMediaDate(),
        episodes = dto.numEpisodes,
        duration = dto.averageEpisodeDuration?.let { it / 60 }, // Convert seconds to minutes
        studio = null,
        trackEntry = this.listStatus?.toTrackEntryDomain(
            mediaId = dto.id ?: 0,
            totalProgress = dto.numEpisodes
        )
    )
}


// ==========================================
// 2. MANGA MAPPERS
// ==========================================

fun MalNode<MalMangaDto>.toMangaDomain(): Manga? {
    val dto = this.node ?: return null

    return Manga(
        id = dto.id ?: 0,
        source = ProviderType.MYANIMELIST,
        title = MediaTitle(
            romaji = dto.title ?: "Unknown",
            english = dto.alternativeTitles?.en,
            native = dto.alternativeTitles?.ja
        ),
        coverImage = dto.mainPicture?.large ?: dto.mainPicture?.medium ?: "",
        bannerImage = null,
        description = dto.synopsis,
        genres = dto.genres?.mapNotNull { it.name } ?: emptyList(),
        status = dto.status.toMediaReleaseStatus(),
        score = dto.mean?.toFloat(),
        startDate = dto.startDate.toMediaDate(),
        chapters = dto.numChapters,
        volumes = dto.numVolumes,
        author = null,
        trackEntry = this.listStatus?.toTrackEntryDomain(
            mediaId = dto.id ?: 0,
            totalProgress = dto.numChapters
        )
    )
}

fun MalAnimeDto.toAnimeDetailsDomain(jikanCharacters: List<JikanCharacterEdge>? = null): AnimeDetails {
    return AnimeDetails(
        id = this.id ?: 0,
        source = ProviderType.MYANIMELIST,
        title = MediaTitle(
            romaji = this.title,
            english = this.alternativeTitles?.en,
            native = this.alternativeTitles?.ja
        ),
        coverImage = this.mainPicture?.large ?: this.mainPicture?.medium ?: "",
        bannerImage = null, // MAL doesn't provide banners natively
        description = this.synopsis ?: "No description available.",
        status = this.status,
        format = mediaType.toMediaFormat(), // "tv" -> MediaFormat.TV
        genres = this.genres?.mapNotNull { it.name } ?: emptyList(),
        averageScore = this.mean?.let { it * 10 }, // Convert MAL's 10-point scale to 100-point scale!

        trailer = null, // Not provided by MAL API v2
        externalLinks = emptyList(), // Not provided by MAL API v2
        characters = jikanCharacters?.mapNotNull { it.toDomain() } ?: emptyList(),

        // Merge related anime and manga into a single list
        relations = buildMalRelations(this.relatedAnime, MediaType.ANIME) +
                buildMalRelations(this.relatedManga, MediaType.MANGA),

        totalEpisodes = this.numEpisodes,
        nextAiringEpisode = null, // Not easily available in basic MAL API

        trackEntry = this.myListStatus?.let { status ->
            TrackEntry(
                entryId = this.id ?: 0,
                mediaId = this.id ?: 0,
                status = status.status.toDomainTrackStatus(),
                progress = status.numEpisodesWatched ?: 0,
                score = status.score?.toDouble(),
                totalProgress = this.numEpisodes
            )
        }
    )
}

fun MalMangaDto.toMangaDetailsDomain(jikanCharacters: List<JikanCharacterEdge>? = null): MangaDetails {
    return MangaDetails(
        id = this.id ?: 0,
        source = ProviderType.MYANIMELIST,
        title = MediaTitle(
            romaji = this.title,
            english = this.alternativeTitles?.en,
            native = this.alternativeTitles?.ja
        ),
        coverImage = this.mainPicture?.large ?: this.mainPicture?.medium ?: "",
        bannerImage = null,
        description = this.synopsis ?: "No description available.",
        status = this.status,
        format = mediaType.toMediaFormat(),
        genres = this.genres?.mapNotNull { it.name } ?: emptyList(),
        averageScore = this.mean?.let { it * 10 },

        externalLinks = emptyList(),
        characters = jikanCharacters?.mapNotNull { it.toDomain() } ?: emptyList(),

        relations = buildMalRelations(this.relatedAnime, MediaType.ANIME) +
                buildMalRelations(this.relatedManga, MediaType.MANGA),

        totalChapters = this.numChapters,
        totalVolumes = this.numVolumes,

        trackEntry = this.myListStatus?.let { status ->
            TrackEntry(
                entryId = this.id ?: 0,
                mediaId = this.id ?: 0,
                status = status.status.toDomainTrackStatus(),
                progress = status.numChaptersRead ?: 0,
                score = status.score?.toDouble(),
                totalProgress = this.numChapters
            )
        }
    )
}

private fun buildMalRelations(edges: List<MalRelatedEdge>?, type: MediaType): List<MediaRelation> {
    return edges?.mapNotNull { edge ->
        val node = edge.node ?: return@mapNotNull null
        MediaRelation(
            id = node.id ?: return@mapNotNull null,
            relationType = RelationType.fromString(edge.relationType),
            title = MediaTitle(romaji = node.title, english = null, native = null),
            coverImage = node.mainPicture?.large ?: node.mainPicture?.medium,
            mediaType = type,
            format = MediaFormat.UNKNOWN // MAL relations payload doesn't return the format directly
        )
    } ?: emptyList()
}

// ==========================================
// 3. TRACKER & DATE PARSING
// ==========================================

fun MalListStatus.toTrackEntryDomain(mediaId: Int, totalProgress: Int? = null): TrackEntry {
    return TrackEntry(
        entryId = mediaId, // MAL uses MediaId for tracking modifications
        mediaId = mediaId,
        status = this.status.toDomainTrackStatus(),
        progress = this.numEpisodesWatched ?: this.numChaptersRead ?: 0,
        score = this.score?.toDouble(),
        totalProgress = totalProgress
    )
}

/**
 * Parses MAL's string dates into your domain MediaDate.
 * MAL dates can be "YYYY-MM-DD", "YYYY-MM", or just "YYYY".
 */
fun String?.toMediaDate(): MediaDate? {
    if (this.isNullOrBlank()) return null
    val parts = this.split("-")
    return MediaDate(
        year = parts.getOrNull(0)?.toIntOrNull(),
        month = parts.getOrNull(1)?.toIntOrNull(),
        day = parts.getOrNull(2)?.toIntOrNull()
    )
}

// ==========================================
// 4. STATUS ENUM MAPPER
// ==========================================

/**
 * Maps MAL's unique API release string to the Domain Enum
 */
fun String?.toMediaReleaseStatus(): MediaReleaseStatus {
    return when (this) {
        "currently_airing", "currently_publishing" -> MediaReleaseStatus.RELEASING
        "finished_airing", "finished" -> MediaReleaseStatus.FINISHED
        "not_yet_aired", "not_yet_published" -> MediaReleaseStatus.NOT_YET_RELEASED
        "on_hiatus" -> MediaReleaseStatus.HIATUS
        "discontinued" -> MediaReleaseStatus.CANCELLED
        else -> MediaReleaseStatus.UNKNOWN
    }
}

fun String?.toMediaFormat(): MediaFormat {
    return when (this) {
        "tv" -> MediaFormat.TV
        "tv_short" -> MediaFormat.TV_SHORT
        "movie" -> MediaFormat.MOVIE
        "special" -> MediaFormat.SPECIAL
        "ova" -> MediaFormat.OVA
        "ona" -> MediaFormat.ONA
        "music" -> MediaFormat.MUSIC
        "manga" -> MediaFormat.MANGA
        "novel" -> MediaFormat.NOVEL
        "one_shot" -> MediaFormat.ONE_SHOT
        else -> MediaFormat.UNKNOWN
    }
}

fun String?.toDomainTrackStatus(): TrackStatus {
    return when (this) {
        "watching", "reading" -> TrackStatus.CURRENT
        "completed" -> TrackStatus.COMPLETED
        "on_hold" -> TrackStatus.PAUSED
        "dropped" -> TrackStatus.DROPPED
        "plan_to_watch", "plan_to_read" -> TrackStatus.PLANNING
        else -> TrackStatus.UNKNOWN
    }
}

// Used for API Requests
fun TrackStatus.toMalAnimeStatus(): String {
    return when (this) {
        TrackStatus.CURRENT -> "watching"
        TrackStatus.COMPLETED -> "completed"
        TrackStatus.PAUSED -> "on_hold"
        TrackStatus.DROPPED -> "dropped"
        TrackStatus.PLANNING -> "plan_to_watch"
        else -> "plan_to_watch"
    }
}

// Used for API Requests
fun TrackStatus.toMalMangaStatus(): String {
    return when (this) {
        TrackStatus.CURRENT -> "reading"
        TrackStatus.COMPLETED -> "completed"
        TrackStatus.PAUSED -> "on_hold"
        TrackStatus.DROPPED -> "dropped"
        TrackStatus.PLANNING -> "plan_to_read"
        else -> "plan_to_read"
    }
}


fun MangaCategory.toMalRankingType(): String? {
    return when (this) {
        MangaCategory.TOP_RATED -> "all"         // MAL's top manga by score
        MangaCategory.POPULAR -> "bypopularity"  // Most members
        MangaCategory.TRENDING -> "bypopularity" // MAL lacks 'trending', popularity is the closest proxy
        MangaCategory.MANHWA -> "manhwa"         // MAL has a specific ranking for Manhwa
        MangaCategory.NEWLY_ADDED -> null        // MAL ranking API does not support "Newly Added"
    }
}