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

// ========================================================================
// MEDIA LIST MAPPER
// ========================================================================

fun MalNode<MalMediaDto>.toMediaDomain(
    mediaType: MediaType
): Media? {

    val dto = node ?: return null

    return Media(
        // =========================================================
        // CORE
        // =========================================================

        id = dto.id ?: 0,

        mediaType = mediaType,

        source = ProviderType.MYANIMELIST,

        format = dto.mediaType.toMediaFormat(),

        title = MediaTitle(
            romaji = dto.title ?: "Unknown",
            english = dto.alternativeTitles?.en,
            native = dto.alternativeTitles?.ja
        ),

        // =========================================================
        // VISUALS
        // =========================================================

        coverImage = dto.mainPicture?.large
            ?: dto.mainPicture?.medium
            ?: "",

        bannerImage = null,

        // =========================================================
        // CONTENT
        // =========================================================

        description = dto.synopsis,

        genres = dto.genres
            ?.mapNotNull { it.name }
            ?: emptyList(),

        tags = emptyList(), // MAL has no tag system

        // =========================================================
        // METADATA
        // =========================================================

        status = dto.status.toMediaReleaseStatus(),

        score = dto.mean?.toFloat(),

        popularity = dto.popularity
            ?: dto.numListUsers,

        favourites = null, // MAL API doesn't expose this

        rank = dto.rank,

        startDate = dto.startDate.toMediaDate(),

        // =========================================================
        // ANIME FIELDS
        // =========================================================

        episodes = dto.numEpisodes,

        duration = dto.averageEpisodeDuration
            ?.div(60),

        studio = dto.studios
            ?.firstOrNull()
            ?.name,

        // =========================================================
        // MANGA FIELDS
        // =========================================================

        chapters = dto.numChapters,

        volumes = dto.numVolumes,

        author = dto.authors
            ?.firstOrNull()
            ?.node
            ?.let { author ->

                listOf(
                    author.firstName,
                    author.lastName
                )
                    .filterNotNull()
                    .joinToString(" ")
                    .ifBlank { null }
            },

        // =========================================================
        // TRACKING
        // =========================================================

//        trackEntry = listStatus?.toTrackEntryDomain(
//            mediaId = dto.id ?: 0,
//            totalProgress = when (mediaType) {
//                MediaType.ANIME -> dto.numEpisodes
//                MediaType.MANGA -> dto.numChapters
//                MediaType.UNKNOWN -> null
//            }
//        )
    )
}

// ========================================================================
// MEDIA DETAILS MAPPER
// ========================================================================

fun MalMediaDto.toMediaDetailsDomain(
    mediaType: MediaType,
    jikanCharacters: List<MediaCharacter>? = null
): MediaDetails {

    val mediaId = id ?: 0

    return MediaDetails(

        // ----------------------------------------------------------------
        // CORE
        // ----------------------------------------------------------------

        id = mediaId,

        mediaType = mediaType,

        source = ProviderType.MYANIMELIST,

        title = MediaTitle(
            romaji = title,
            english = alternativeTitles?.en,
            native = alternativeTitles?.ja
        ),

        synonyms = alternativeTitles?.synonyms ?: emptyList(),

        countryOfOrigin = null,

        // ----------------------------------------------------------------
        // VISUALS
        // ----------------------------------------------------------------

        coverImage = mainPicture?.large
            ?: mainPicture?.medium
            ?: "",

        bannerImage = null,

        extraPictures = pictures
            ?.mapNotNull { it.large ?: it.medium }
            ?: emptyList(),

        description = synopsis ?: "No description available.",

        background = background,

        // ----------------------------------------------------------------
        // METADATA
        // ----------------------------------------------------------------

        status = status.toMediaReleaseStatus(),

        format = this.mediaType.toMediaFormat(),

        sourceMaterial = source.toMediaSourceMaterial(),

        isAdult = nsfw == "black" || rating == "rx",

        // ----------------------------------------------------------------
        // DATES
        // ----------------------------------------------------------------

        startDate = startDate?.toFuzzyDate(),

        endDate = endDate?.toFuzzyDate(),

        createdAt = createdAt.toMediaDate(),

        updatedAt = updatedAt.toMediaDate(),

        season = startSeason
            ?.season
            ?.toMediaSeason(),

        seasonYear = startSeason?.year,

        broadcastString = broadcast?.let {
            "${it.dayOfTheWeek?.replaceFirstChar(Char::uppercase)} at ${it.startTime}"
        },

        // ----------------------------------------------------------------
        // CATEGORIZATION
        // ----------------------------------------------------------------

        genres = genres
            ?.mapNotNull { it.name }
            ?: emptyList(),

        tags = emptyList(),

        // ----------------------------------------------------------------
        // STATS
        // ----------------------------------------------------------------

        averageScore = mean?.let { it * 10 },

        meanScore = mean,

        popularity = popularity ?: numListUsers,

        favourites = null,

        rank = rank,

        // ----------------------------------------------------------------
        // ANIME FIELDS
        // ----------------------------------------------------------------

        totalEpisodes = numEpisodes,

        durationPerEpisode = averageEpisodeDuration
            ?.div(60),

        studios = if (mediaType == MediaType.ANIME) {
            studios?.map {
                MediaStudio(
                    id = it.id ?: 0,
                    name = it.name ?: "Unknown",
                    isAnimationStudio = true
                )
            } ?: emptyList()
        } else {
            emptyList()
        },

        nextAiringEpisode = null,

        contentRating = rating?.uppercase(),

        // ----------------------------------------------------------------
        // MANGA FIELDS
        // ----------------------------------------------------------------

        totalChapters = numChapters,

        totalVolumes = numVolumes,

        serializations = if (mediaType == MediaType.MANGA) {
            serialization
                ?.mapNotNull { it.node?.name }
                ?: emptyList()
        } else {
            emptyList()
        },

        authors = if (mediaType == MediaType.MANGA) {
            authors?.map { edge ->

                val fullName = listOf(
                    edge.node?.firstName,
                    edge.node?.lastName
                )
                    .filterNotNull()
                    .joinToString(" ")

                MediaStaff(
                    id = edge.node?.id ?: 0,
                    name = fullName.ifBlank { "Unknown" },
                    role = edge.role ?: "Author"
                )
            } ?: emptyList()
        } else {
            emptyList()
        },

        // ----------------------------------------------------------------
        // SHARED STAFF
        // ----------------------------------------------------------------

        staff = if (mediaType == MediaType.MANGA) {
            authors?.map { edge ->

                val fullName = listOf(
                    edge.node?.firstName,
                    edge.node?.lastName
                )
                    .filterNotNull()
                    .joinToString(" ")

                MediaStaff(
                    id = edge.node?.id ?: 0,
                    name = fullName.ifBlank { "Unknown" },
                    role = edge.role ?: "Author"
                )
            } ?: emptyList()
        } else {
            emptyList()
        },

        // ----------------------------------------------------------------
        // RELATIONS
        // ----------------------------------------------------------------

        trailer = null,

        externalLinks = emptyList(),

        characters = jikanCharacters ?: emptyList(),

        relations =
            buildMalRelations(
                relatedAnime,
                MediaType.ANIME
            ) +
                    buildMalRelations(
                        relatedManga,
                        MediaType.MANGA
                    ),

        // ----------------------------------------------------------------
        // USER TRACKING
        // ----------------------------------------------------------------

//        trackEntry = myListStatus?.let { status ->
//
//            TrackEntry(
//                entryId = mediaId,
//
//                mediaId = mediaId,
//
//                status = status.status.toDomainTrackStatus(),
//
//                progress = when (mediaType) {
//                    MediaType.ANIME ->
//                        status.numEpisodesWatched ?: 0
//
//                    MediaType.MANGA ->
//                        status.numChaptersRead ?: 0
//
//                    MediaType.UNKNOWN -> 0
//                },
//
//                score = status.score,
//
//                totalProgress = when (mediaType) {
//                    MediaType.ANIME -> numEpisodes
//                    MediaType.MANGA -> numChapters
//                    MediaType.UNKNOWN -> null
//                }
//            )
//        },

        // ----------------------------------------------------------------
        // USER STATS
        // ----------------------------------------------------------------

        watching = statistics?.status?.watching,

        completed = statistics?.status?.completed,

        onHold = statistics?.status?.onHold,

        dropped = statistics?.status?.dropped,

        planToWatch = statistics?.status?.planToWatch,
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


fun MediaCategory.toMalRankingType(
    mediaType: MediaType
): String? {

    return when (mediaType) {

        // =====================================================================================
        // ANIME
        // =====================================================================================

        MediaType.ANIME -> when (this) {

            MediaCategory.TRENDING ->
                "all"

            MediaCategory.POPULAR ->
                "bypopularity"

            MediaCategory.TOP_RATED ->
                "all"

            MediaCategory.UPCOMING ->
                "upcoming"

            MediaCategory.SEASONAL ->
                null // handled by seasonal endpoint

            MediaCategory.MOVIES ->
                "movie"

            // unsupported
            MediaCategory.NEWLY_ADDED,
            MediaCategory.MANHWA,
            MediaCategory.NOVELS ->
                null
        }

        // =====================================================================================
        // MANGA
        // =====================================================================================

        MediaType.MANGA -> when (this) {

            MediaCategory.TRENDING ->
                "manga"

            MediaCategory.POPULAR ->
                "bypopularity"

            MediaCategory.TOP_RATED ->
                "manga"

            MediaCategory.NEWLY_ADDED ->
                "publishing"

            MediaCategory.MANHWA ->
                null // MAL has no dedicated ranking

            MediaCategory.NOVELS ->
                "novels"

            // unsupported
            MediaCategory.UPCOMING,
            MediaCategory.SEASONAL,
            MediaCategory.MOVIES ->
                null
        }

        else -> null
    }
}