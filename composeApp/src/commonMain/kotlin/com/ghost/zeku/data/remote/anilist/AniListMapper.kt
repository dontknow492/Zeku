package com.ghost.zeku.data.remote.anilist

import com.ghost.zeku.data.remote.anilist.model.*
import com.ghost.zeku.data.remote.anilist.model.GraphQLRequest.Variables
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.media.MediaDate
import com.ghost.zeku.domain.model.media.MediaTitle
import com.ghost.zeku.domain.model.media.track.TrackEntry
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.filter.search.SearchSort
import com.ghost.zeku.domain.model.media.track.TrackStatus
import java.util.*

// =========================================================================================
// MAIN MAPPERS
// =========================================================================================
fun AniListMedia.toMediaDomain(): Media {
    return Media(

        // --------------------------------------------------------------------
        // Identity
        // --------------------------------------------------------------------

        id = id,

        source = ProviderType.ANILIST,

        mediaType = MediaType.fromString(type),

        // --------------------------------------------------------------------
        // Core
        // --------------------------------------------------------------------

        format = format.toMediaFormat(),

        title = title.toDomain(),

        coverImage = coverImage?.large.orEmpty(),

        bannerImage = bannerImage,

        description = description,

        genres = genres ?: emptyList(),

        status = mapToMediaReleaseStatus(status),

        score = averageScore?.toFloat(),

        startDate = startDate?.toMediaDate(),

        // --------------------------------------------------------------------
        // Extended
        // --------------------------------------------------------------------

        tags = tags
            ?.map { it.name }
            ?: emptyList(),

        popularity = popularity,

        favourites = favourites,

        rank = null,

        // --------------------------------------------------------------------
        // Anime
        // --------------------------------------------------------------------

        episodes = episodes,

        duration = duration,

        studio = studios
            ?.edges
            ?.firstOrNull { it.isMain == true }
            ?.node
            ?.name,

        // --------------------------------------------------------------------
        // Manga
        // --------------------------------------------------------------------

        chapters = chapters,

        volumes = volumes,

        author = staff
            ?.edges
            ?.firstOrNull()
            ?.node
            ?.name
            ?.userPreferred
    )
}


// =========================================================================================
// DETAILS MAPPERS (If you still wish to keep DetailedMedia.kt)
// =========================================================================================

fun AniListMedia.toMediaDetailsDomain(): MediaDetails {
    return MediaDetails(

        // --------------------------------------------------------------------
        // Identity
        // --------------------------------------------------------------------

        id = id,

        source = ProviderType.ANILIST,

        mediaType = MediaType.fromString(type),

        // --------------------------------------------------------------------
        // Core
        // --------------------------------------------------------------------

        title = title?.toDomain() ?: MediaTitle(),

        synonyms = synonyms ?: emptyList(),

        countryOfOrigin = countryOfOrigin,

        // --------------------------------------------------------------------
        // Visuals
        // --------------------------------------------------------------------

        coverImage = coverImage?.extraLarge
            ?: coverImage?.large
            ?: "",

        bannerImage = bannerImage,

        extraPictures = emptyList(),

        description = description,

        background = null,

        // --------------------------------------------------------------------
        // Metadata
        // --------------------------------------------------------------------

        status = mapToMediaReleaseStatus(status),

        format = format.toMediaFormat(),

        sourceMaterial = source?.toSourceMaterial(),

        isAdult = isAdult ?: false,

        // --------------------------------------------------------------------
        // Dates
        // --------------------------------------------------------------------

        startDate = startDate?.toMediaDate(),

        endDate = endDate?.toMediaDate(),

        createdAt = null,

        updatedAt = null,

        // --------------------------------------------------------------------
        // Seasonal
        // --------------------------------------------------------------------

        season = season?.toMediaSeason(),

        seasonYear = seasonYear,

        broadcastString = null,

        // --------------------------------------------------------------------
        // Categorization
        // --------------------------------------------------------------------

        genres = genres ?: emptyList(),

        tags = tags
            ?.map { it.toDomain() }
            ?: emptyList(),

        // --------------------------------------------------------------------
        // Statistics
        // --------------------------------------------------------------------

        averageScore = averageScore?.toDouble(),

        meanScore = meanScore?.toDouble(),

        popularity = popularity,

        favourites = favourites,

        rank = null,

        // --------------------------------------------------------------------
        // Anime
        // --------------------------------------------------------------------

        totalEpisodes = episodes,

        durationPerEpisode = duration,

        contentRating = if (isAdult == true) "18+" else null,

        nextAiringEpisode = nextAiringEpisode?.let {
            AiringSchedule(
                episode = it.episode,
                timeUntilAiring = it.timeUntilAiring
            )
        },

        studios = studios
            ?.edges
            ?.mapNotNull { it.node?.toDomain() }
            ?: emptyList(),

        // --------------------------------------------------------------------
        // Manga
        // --------------------------------------------------------------------

        totalChapters = chapters,

        totalVolumes = volumes,

        serializations = emptyList(),

        authors = staff
            ?.edges
            ?.mapNotNull { it.toDomain() }
            ?: emptyList(),

        // --------------------------------------------------------------------
        // Relations
        // --------------------------------------------------------------------

        trailer = trailer?.let {
            MediaTrailer(
                title = null,
                id = it.id.orEmpty(),
                site = it.site.orEmpty(),
                thumbnail = it.thumbnail
            )
        },

        externalLinks = externalLinks
            ?.map {
                ExternalLink(
                    url = it.url,
                    site = it.site,
                    iconUrl = it.icon
                )
            }
            ?: emptyList(),

        characters = characters
            ?.edges
            ?.mapNotNull { it.toDomain() }
            ?: emptyList(),

        relations = relations
            ?.edges
            ?.mapNotNull { it.toDomain() }
            ?: emptyList(),

        staff = staff
            ?.edges
            ?.mapNotNull { it.toDomain() }
            ?: emptyList(),

        // --------------------------------------------------------------------
        // Stats
        // --------------------------------------------------------------------

        watching = null,

        completed = null,

        onHold = null,

        dropped = null,

        planToWatch = null
    )
}


fun AniListDate.toMediaDate() = MediaDate(year = this.year, month = this.month, day = this.day)


fun AniListTag.toDomain() = MediaTag(
    name = this.name,
    description = this.description,
    rank = this.rank,
    isSpoiler = this.isMediaSpoiler ?: false,
    category = this.category
)

fun AniListStudioNode.toDomain() = MediaStudio(
    id = this.id,
    name = this.name,
    isAnimationStudio = this.isAnimationStudio
)

fun AniListStaffEdge.toDomain(): MediaStaff? {
    val node = this.node ?: return null
    return MediaStaff(
        id = node.id,
        name = node.name?.userPreferred ?: "Unknown",
        role = this.role ?: "Unknown Role",
        imageUrl = node.image?.large
    )
}

fun AniListCharacterEdge.toDomain(): MediaCharacter? {
    val node = this.node ?: return null
    return MediaCharacter(
        id = node.id ?: return null,
        name = node.name?.full ?: "Unknown",
        imageUrl = node.image?.large,

        // SAFE CONVERSION: "MAIN" -> CharacterRole.MAIN
        role = CharacterRole.fromString(this.role)
    )
}

fun AniListRelationEdge.toDomain(): MediaRelation? {
    val node = this.node ?: return null
    return MediaRelation(
        id = node.id,
        relationType = runCatching { RelationType.valueOf(this.relationType ?: "") }.getOrDefault(RelationType.OTHER),
        title = node.title?.toDomain() ?: MediaTitle(),
        coverImage = node.coverImage?.large,
        mediaType = if (node.type == "ANIME") MediaType.ANIME else MediaType.MANGA,
        format = runCatching { MediaFormat.valueOf(node.format ?: "") }.getOrDefault(MediaFormat.UNKNOWN)
    )
}

private fun AniListAiringSchedule.toDomain(): AiringSchedule? {
    if (this.episode == null || this.timeUntilAiring == null) return null
    return AiringSchedule(this.episode, this.timeUntilAiring)
}

// --- Lazy Load Mappers ---

fun AniListReviewNode.toDomain(): Review {
    return Review(
        id = this.id ?: 0,
        author = this.user?.name ?: "Anonymous",
        authorAvatar = this.user?.avatar?.large,
        score = this.score ?: this.rating,
        summary = this.summary,
        body = this.body ?: "",
        upvotes = this.ratingAmount ?: 0,
        isSpoiler = false, // AniList API handles spoilers via tags in the body text usually
        createdAt = this.createdAt?.times(1000L) // Convert seconds to milliseconds
    )
}

// =========================================================================================
// HELPER EXTENSIONS (Keeps main mappers clean)
// =========================================================================================

private fun AniListTitle?.toDomain(): MediaTitle {
    return MediaTitle(
        romaji = this?.romaji,
        english = this?.english,
        native = this?.native,
        userPreferred = this?.userPreferred,
    )
}

private fun AniListDate?.toDomain(): MediaDate? {
    if (this == null) return null
    return MediaDate(year = this.year, month = this.month, day = this.day)
}

fun AniListMediaListEntry.toTrackEntry(
    mediaId: Int,
    totalProgress: Int? // Required for the progress bar calculation you added!
): TrackEntry {
    return TrackEntry(
        entryId = this.id,
        mediaId = mediaId,
        status = mapToDomainTrackStatus(this.status),
        progress = this.progress ?: 0,
        score = this.score,
        totalProgress = totalProgress
    )
}

fun Viewer.toDomain(): UserProfile = UserProfile(
    id = this.id.toString(),
    source = ProviderType.ANILIST,
    username = this.name,
    avatarUrl = this.avatar.large,
    bannerUrl = this.bannerImage
)


// =========================================================================================
// USER LIST MAPPERS (The Bridge)
// =========================================================================================


// =========================================================================================
// ENUM MAPPERS
// =========================================================================================

/**
 * Maps the User's list status (e.g., Currently Watching, Planning to Read)
 */
fun mapToDomainTrackStatus(statusStr: String?): TrackStatus {
    return when (statusStr?.uppercase()) {
        "CURRENT" -> TrackStatus.CURRENT
        "PLANNING" -> TrackStatus.PLANNING
        "COMPLETED" -> TrackStatus.COMPLETED
        "DROPPED" -> TrackStatus.DROPPED
        "PAUSED" -> TrackStatus.PAUSED
        "REPEATING" -> TrackStatus.REPEATING
        else -> TrackStatus.UNKNOWN
    }
}

fun mapDomainToTrackStatus(status: TrackStatus): String {
    return when (status) {
        TrackStatus.CURRENT -> "CURRENT"
        TrackStatus.PLANNING -> "PLANNING"
        TrackStatus.COMPLETED -> "COMPLETED"
        TrackStatus.DROPPED -> "DROPPED"
        TrackStatus.PAUSED -> "PAUSED"
        TrackStatus.REPEATING -> "REPEATING"
        else -> "CURRENT"
    }
}

/**
 * Maps the Anime/Manga's global release status (e.g., Releasing, Finished)
 */
private fun mapToMediaReleaseStatus(statusStr: String?): MediaReleaseStatus {
    return when (statusStr?.uppercase()) {
        "FINISHED" -> MediaReleaseStatus.FINISHED
        "RELEASING" -> MediaReleaseStatus.RELEASING
        "NOT_YET_RELEASED" -> MediaReleaseStatus.NOT_YET_RELEASED
        "CANCELLED" -> MediaReleaseStatus.CANCELLED
        "HIATUS" -> MediaReleaseStatus.HIATUS
        else -> MediaReleaseStatus.UNKNOWN
    }
}


fun MediaCategory.toAniListVariables(
    mediaType: MediaType,
    page: Int,
    perPage: Int
): Variables {

    return when (this) {

        MediaCategory.TRENDING -> {
            Variables(
                page = page,
                perPage = perPage,
                type = mediaType.name,
                sort = listOf(
                    "TRENDING_DESC",
                    "POPULARITY_DESC"
                )
            )
        }

        MediaCategory.POPULAR -> {
            Variables(
                page = page,
                perPage = perPage,
                type = mediaType.name,
                sort = listOf("POPULARITY_DESC")
            )
        }

        MediaCategory.TOP_RATED -> {
            Variables(
                page = page,
                perPage = perPage,
                type = mediaType.name,
                sort = listOf("SCORE_DESC")
            )
        }

        MediaCategory.UPCOMING -> {
            Variables(
                page = page,
                perPage = perPage,
                type = mediaType.name,
                status = "NOT_YET_RELEASED",
                sort = listOf("POPULARITY_DESC")
            )
        }

        MediaCategory.SEASONAL -> {

            val (season, year) = getCurrentSeasonAndYear()

            Variables(
                page = page,
                perPage = perPage,
                type = mediaType.name,
                season = season,
                seasonYear = year,
                sort = listOf(
                    "POPULARITY_DESC",
                    "TRENDING_DESC"
                )
            )
        }

        MediaCategory.NEWLY_ADDED -> {
            Variables(
                page = page,
                perPage = perPage,
                type = mediaType.name,
                sort = listOf("START_DATE_DESC")
            )
        }

        MediaCategory.MOVIES -> {
            Variables(
                page = page,
                perPage = perPage,
                type = MediaType.ANIME.name,
                format = "MOVIE",
                sort = listOf(
                    "POPULARITY_DESC",
                    "SCORE_DESC"
                )
            )
        }

        MediaCategory.MANHWA -> {
            Variables(
                page = page,
                perPage = perPage,
                type = MediaType.MANGA.name,
                countryOfOrigin = "KR",
                sort = listOf(
                    "TRENDING_DESC",
                    "POPULARITY_DESC"
                )
            )
        }

        MediaCategory.NOVELS -> {
            Variables(
                page = page,
                perPage = perPage,
                type = MediaType.MANGA.name,
                format = "NOVEL",
                sort = listOf(
                    "POPULARITY_DESC",
                    "SCORE_DESC"
                )
            )
        }
    }
}


fun SearchSort.toAniListSort(): String {
    return when (this) {
        SearchSort.TRENDING_DESC -> "TRENDING_DESC"
        SearchSort.POPULARITY_DESC -> "POPULARITY_DESC"
        SearchSort.SCORE_DESC -> "SCORE_DESC"
        SearchSort.START_DATE_DESC -> "START_DATE_DESC"
        SearchSort.ALPHABETICAL_ASC -> "TITLE_ROMAJI"
    }
}

fun String?.toMediaFormat(): MediaFormat {
    return when (this?.uppercase()) {
        "TV" -> MediaFormat.TV
        "TV_SHORT" -> MediaFormat.TV_SHORT
        "MOVIE" -> MediaFormat.MOVIE
        "SPECIAL" -> MediaFormat.SPECIAL
        "OVA" -> MediaFormat.OVA
        "ONA" -> MediaFormat.ONA
        "MUSIC" -> MediaFormat.MUSIC
        "MANGA" -> MediaFormat.MANGA
        "NOVEL" -> MediaFormat.NOVEL
        "ONE_SHOT" -> MediaFormat.ONE_SHOT
        else -> MediaFormat.UNKNOWN
    }
}


fun getCurrentSeasonAndYear(): Pair<String, Int> {
    val calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH) // 0-11
    val year = calendar.get(Calendar.YEAR)

    val season = when (month) {
        in 0..2 -> "WINTER"
        in 3..5 -> "SPRING"
        in 6..8 -> "SUMMER"
        else -> "FALL"
    }
    return season to year
}


private fun String.toSourceMaterial(): MediaSourceMaterial {
    return runCatching { MediaSourceMaterial.valueOf(this) }.getOrDefault(MediaSourceMaterial.UNKNOWN)
}

private fun String?.toMediaSeason(): MediaSeason {
    return when (this?.uppercase()) {
        "WINTER" -> MediaSeason.WINTER
        "SUMMER" -> MediaSeason.SUMMER
        "SPRING" -> MediaSeason.SPRING
        "FALL" -> MediaSeason.FALL
        else -> MediaSeason.UNKNOWN
    }
}
