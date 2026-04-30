package com.ghost.zeku.data.remote.mal

import com.ghost.zeku.data.remote.mal.model.*
import com.ghost.zeku.domain.model.UserProfile
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

fun MalAnimeDto.toAnimeDetailsDomain(jikanCharacters: List<MediaCharacter>? = null): AnimeDetails {
    return AnimeDetails(
        id = this.id ?: 0,
        source = ProviderType.MYANIMELIST,
        title = MediaTitle(
            romaji = this.title,
            english = this.alternativeTitles?.en,
            native = this.alternativeTitles?.ja
        ),
        synonyms = this.alternativeTitles?.synonyms ?: emptyList(),
        countryOfOrigin = null, // MAL doesn't natively supply this, assume JP generally

        coverImage = this.mainPicture?.large ?: this.mainPicture?.medium ?: "",
        bannerImage = null, // MAL doesn't provide banners natively
        extraPictures = this.pictures?.mapNotNull { it.large ?: it.medium } ?: emptyList(),
        description = this.synopsis ?: "No description available.",
        background = this.background,

        status = this.status.toMediaReleaseStatus(),
        format = this.mediaType.toMediaFormat(),
        sourceMaterial = this.source.toMediaSourceMaterial(),
        isAdult = this.nsfw == "black" || this.rating == "rx", // 'black' usually means Hentai/Rx on MAL

        startDate = this.startDate?.toFuzzyDate(),
        endDate = this.endDate?.toFuzzyDate(),
        season = this.startSeason?.season?.toMediaSeason(),
        seasonYear = this.startSeason?.year,
        broadcastString = this.broadcast?.let { "${it.dayOfTheWeek?.replaceFirstChar { c -> c.uppercase() }} at ${it.startTime}" },

        genres = this.genres?.mapNotNull { it.name } ?: emptyList(),
        tags = emptyList(), // MAL doesn't use the tag system, demographics are in genres

        averageScore = this.mean?.let { it * 10 }, // Convert 8.5 to 85.0
        meanScore = this.mean, // Keep original 10-point scale for reference
        popularity = this.numListUsers, // numListUsers is exactly what 'Popularity' means
        favourites = null, // MAL v2 doesn't expose favourites in the basic endpoint
        rank = this.rank,

        totalEpisodes = this.numEpisodes,
        durationPerEpisode = this.averageEpisodeDuration?.let { it / 60 }, // Seconds to minutes
        contentRating = this.rating?.uppercase(),
        nextAiringEpisode = null, // Not easily available in basic MAL API
        studios = this.studios?.map { MediaStudio(it.id ?: 0, it.name ?: "Unknown", true) } ?: emptyList(),

        trailer = null, // Not provided by MAL API v2
        externalLinks = emptyList(), // Not provided by MAL API v2
        characters = jikanCharacters ?: emptyList(), // Injecting Jikan here!
        relations = buildMalRelations(this.relatedAnime, MediaType.ANIME) + buildMalRelations(
            this.relatedManga,
            MediaType.MANGA
        ),
        staff = emptyList(), // Jikan enrichment would be needed for staff

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

fun MalMangaDto.toMangaDetailsDomain(jikanCharacters: List<MediaCharacter>? = null): MangaDetails {
    return MangaDetails(
        id = this.id ?: 0,
        source = ProviderType.MYANIMELIST,
        title = MediaTitle(
            romaji = this.title,
            english = this.alternativeTitles?.en,
            native = this.alternativeTitles?.ja
        ),
        synonyms = this.alternativeTitles?.synonyms ?: emptyList(),
        countryOfOrigin = null,

        coverImage = this.mainPicture?.large ?: this.mainPicture?.medium ?: "",
        bannerImage = null,
        extraPictures = this.pictures?.mapNotNull { it.large ?: it.medium } ?: emptyList(),
        description = this.synopsis ?: "No description available.",
        background = this.background,

        status = this.status.toMediaReleaseStatus(),
        format = this.mediaType.toMediaFormat(),
        sourceMaterial = MediaSourceMaterial.MANGA, // Self-evident for manga endpoint
        isAdult = this.nsfw == "black",

        startDate = this.startDate?.toFuzzyDate(),
        endDate = this.endDate?.toFuzzyDate(),

        genres = this.genres?.mapNotNull { it.name } ?: emptyList(),
        tags = emptyList(),

        averageScore = this.mean?.let { it * 10 },
        meanScore = this.mean,
        popularity = this.numListUsers,
        favourites = null,
        rank = this.rank,

        totalChapters = this.numChapters,
        totalVolumes = this.numVolumes,
        serializations = this.serialization?.mapNotNull { it.node?.name } ?: emptyList(),

        authors = this.authors?.map { edge ->
            val firstName = edge.node?.firstName ?: ""
            val lastName = edge.node?.lastName ?: ""
            val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            MediaStaff(id = edge.node?.id ?: 0, name = fullName.ifBlank { "Unknown" }, role = edge.role ?: "Author")
        } ?: emptyList(),

        externalLinks = emptyList(),
        characters = jikanCharacters ?: emptyList(), // Injecting Jikan here!
        relations = buildMalRelations(this.relatedAnime, MediaType.ANIME) + buildMalRelations(
            this.relatedManga,
            MediaType.MANGA
        ),

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


/**
 * Safely parses MAL date strings ("YYYY-MM-DD", "YYYY-MM", or "YYYY")
 */
fun String.toFuzzyDate(): MediaDate {
    val parts = this.split("-")
    return MediaDate(
        year = parts.getOrNull(0)?.toIntOrNull(),
        month = parts.getOrNull(1)?.toIntOrNull(),
        day = parts.getOrNull(2)?.toIntOrNull()
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


fun MalUserDto.toDomain(): UserProfile {
    return UserProfile(
        id = this.id.toString(),
        source = ProviderType.MYANIMELIST,
        username = this.name,
        avatarUrl = this.pictureUrl,
        bannerUrl = null
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
private fun String?.toMediaReleaseStatus(): MediaReleaseStatus {
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


private fun String?.toMediaSourceMaterial(): MediaSourceMaterial {
    return when (this) {
        "original" -> MediaSourceMaterial.ORIGINAL
        "manga" -> MediaSourceMaterial.MANGA
        "light_novel" -> MediaSourceMaterial.LIGHT_NOVEL
        "visual_novel" -> MediaSourceMaterial.VISUAL_NOVEL
        "game" -> MediaSourceMaterial.VIDEO_GAME
        "other" -> MediaSourceMaterial.OTHER
        "novel" -> MediaSourceMaterial.NOVEL
        "doujinshi" -> MediaSourceMaterial.DOUJINSHI
        "web_manga" -> MediaSourceMaterial.MANGA
        "picture_book" -> MediaSourceMaterial.PICTURE_BOOK
        else -> MediaSourceMaterial.UNKNOWN
    }
}


private fun String?.toMediaSeason(): MediaSeason {
    return when (this) {
        "winter" -> MediaSeason.WINTER
        "spring" -> MediaSeason.SPRING
        "summer" -> MediaSeason.SUMMER
        "fall" -> MediaSeason.FALL
        else -> MediaSeason.UNKNOWN
    }
}