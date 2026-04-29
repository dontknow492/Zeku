package com.ghost.zeku.data.remote.anilist

import com.ghost.zeku.data.remote.anilist.model.*
import com.ghost.zeku.data.remote.anilist.model.GraphQLRequest.Variables
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.*
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.domain.model.search.SearchSort

// =========================================================================================
// MAIN MAPPERS
// =========================================================================================

fun AniListMedia.toAnimeDomain(): Anime {
    return Anime(
        id = this.id,
        source = ProviderType.ANILIST, // Explicitly declare source
        format = this.format.toMediaFormat(),
        title = this.title.toDomain(),
        coverImage = this.coverImage?.large ?: "",
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres ?: emptyList(),
        status = mapToMediaReleaseStatus(this.status),
        score = this.averageScore?.toFloat(),
        startDate = this.startDate.toDomain(),
        episodes = this.episodes,
        duration = this.duration,
        studio = null, // Add to GraphQL query later if needed
        trackEntry = this.mediaListEntry?.toTrackEntry(
            mediaId = this.id,
            totalProgress = this.episodes // We pass total episodes to calculate the progress bar!
        )
    )
}

fun AniListMedia.toMangaDomain(): Manga {
    return Manga(
        id = this.id,
        source = ProviderType.ANILIST,
        format = this.format.toMediaFormat(),
        title = this.title.toDomain(),
        coverImage = this.coverImage?.large ?: "",
        bannerImage = this.bannerImage,
        description = this.description,
        genres = this.genres ?: emptyList(),
        status = mapToMediaReleaseStatus(this.status),
        score = this.averageScore?.toFloat(),
        startDate = this.startDate.toDomain(),
        chapters = this.chapters,
        volumes = this.volumes,
        author = null,
        trackEntry = this.mediaListEntry?.toTrackEntry(
            mediaId = this.id,
            totalProgress = this.chapters // Pass total chapters for progress bar
        )
    )
}

// =========================================================================================
// DETAILS MAPPERS (If you still wish to keep DetailedMedia.kt)
// =========================================================================================

fun AniListMedia.toAnimeDetailsDomain(): AnimeDetails {
    return AnimeDetails(
        id = this.id,
        source = ProviderType.ANILIST,
        title = this.title.toDomain(), // Assuming you have this mapper already
        coverImage = this.coverImage?.large ?: "",
        bannerImage = this.bannerImage,
        description = this.description,
        status = this.status,
        format = this.format.toMediaFormat(),
        genres = this.genres ?: emptyList(),
        averageScore = this.averageScore?.toDouble(),

        trailer = this.trailer?.toDomain(),
        externalLinks = this.externalLinks?.map { it.toDomain() } ?: emptyList(),
        characters = this.characters?.edges?.mapNotNull { it.toDomain() } ?: emptyList(),
        relations = this.relations?.edges?.mapNotNull { it.toDomain() } ?: emptyList(),

        totalEpisodes = this.episodes,
        nextAiringEpisode = this.nextAiringEpisode?.toDomain(),
        trackEntry = this.mediaListEntry?.toTrackEntry(this.id, this.episodes)
    )
}

fun AniListMedia.toMangaDetailsDomain(): MangaDetails {
    return MangaDetails(
        id = this.id,
        source = ProviderType.ANILIST,
        title = this.title.toDomain(),
        coverImage = this.coverImage?.large ?: "",
        bannerImage = this.bannerImage,
        description = this.description,
        status = this.status,
        format = this.format.toMediaFormat(),
        genres = this.genres ?: emptyList(),
        averageScore = this.averageScore?.toDouble(),

        externalLinks = this.externalLinks?.map { it.toDomain() } ?: emptyList(),
        characters = this.characters?.edges?.mapNotNull { it.toDomain() } ?: emptyList(),
        relations = this.relations?.edges?.mapNotNull { it.toDomain() } ?: emptyList(),

        totalChapters = this.chapters,
        totalVolumes = this.volumes,
        trackEntry = this.mediaListEntry?.toTrackEntry(this.id, this.chapters)
    )
}

// --- Sub-component Mappers ---

private fun AniListTrailer.toDomain() = MediaTrailer(
    id = this.id ?: "",
    site = this.site ?: "unknown",
    thumbnail = this.thumbnail,
    title = null
)

private fun AniListExternalLink.toDomain() = ExternalLink(
    url = this.url ?: "",
    site = this.site ?: "Website",
    iconUrl = this.icon
)

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
        id = node.id ?: return null,

        // SAFE CONVERSIONS
        relationType = RelationType.fromString(this.relationType),
        mediaType = MediaType.fromString(node.type),
        format = node.format.toMediaFormat(),

        title = node.title?.toDomain() ?: return null, // assuming toDomain() exists for Title
        coverImage = node.coverImage?.large
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
        native = this?.native
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


// =========================================================================================
// USER LIST MAPPERS (The Bridge)
// =========================================================================================

/**
 * Maps a User List Entry to an Anime domain model.
 * It combines the entry data with the media data before passing it to the main mapper.
 */
fun AniListMediaListEntry.toAnimeDomain(): Anime? {
    // If for some reason the media is null, we can't show it.
    val actualMedia = this.media ?: return null

    // Inject this entry into the media object so our existing mapper handles everything
    val combinedMedia = actualMedia.copy(mediaListEntry = this)

    return combinedMedia.toAnimeDomain()
}

/**
 * Maps a User List Entry to a Manga domain model.
 */
fun AniListMediaListEntry.toMangaDomain(): Manga? {
    val actualMedia = this.media ?: return null
    val combinedMedia = actualMedia.copy(mediaListEntry = this)
    return combinedMedia.toMangaDomain()
}


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
fun mapToMediaReleaseStatus(statusStr: String?): MediaReleaseStatus {
    return when (statusStr?.uppercase()) {
        "FINISHED" -> MediaReleaseStatus.FINISHED
        "RELEASING" -> MediaReleaseStatus.RELEASING
        "NOT_YET_RELEASED" -> MediaReleaseStatus.NOT_YET_RELEASED
        "CANCELLED" -> MediaReleaseStatus.CANCELLED
        "HIATUS" -> MediaReleaseStatus.HIATUS
        else -> MediaReleaseStatus.UNKNOWN
    }
}


fun MangaCategory.toAniListSort(): List<String> {
    return when (this) {
        MangaCategory.TRENDING -> listOf("TRENDING_DESC", "POPULARITY_DESC")
        MangaCategory.POPULAR -> listOf("POPULARITY_DESC")
        MangaCategory.TOP_RATED -> listOf("SCORE_DESC")
        MangaCategory.NEWLY_ADDED -> listOf("START_DATE_DESC")
        MangaCategory.MANHWA -> listOf("TRENDING_DESC") // You can also add 'countryOfOrigin: "KR"' variables later
    }
}


fun AnimeCategory.toAniListVariables(page: Int, perPage: Int): GraphQLRequest.Variables {
    return when (this) {
        AnimeCategory.TRENDING -> Variables(
            page = page,
            perPage = perPage,
            sort = listOf("TRENDING_DESC", "POPULARITY_DESC")
        )

        AnimeCategory.TOP_RATED -> Variables(
            page = page,
            perPage = perPage,
            sort = listOf("SCORE_DESC")
        )

        AnimeCategory.UPCOMING -> Variables(
            page = page,
            perPage = perPage,
            status = "NOT_YET_RELEASED",
            sort = listOf("POPULARITY_DESC")
        )

        AnimeCategory.MOVIE -> Variables(
            page = page,
            perPage = perPage,
            format = "MOVIE",
            type = "ANIME", // You'd add 'format: MOVIE' to query if needed
            sort = listOf("POPULARITY_DESC")
        )

        AnimeCategory.SEASONAL -> {
            val (season, year) = getCurrentSeasonAndYear()
            Variables(
                page = page,
                perPage = perPage,
                season = season,
                seasonYear = year,
                sort = listOf("POPULARITY_DESC")
            )
        }

        AnimeCategory.POPULAR -> {
            Variables(page = page, perPage = perPage, sort = listOf("POPULARITY_DESC"))
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
    val calendar = java.util.Calendar.getInstance()
    val month = calendar.get(java.util.Calendar.MONTH) // 0-11
    val year = calendar.get(java.util.Calendar.YEAR)

    val season = when (month) {
        in 0..2 -> "WINTER"
        in 3..5 -> "SPRING"
        in 6..8 -> "SUMMER"
        else -> "FALL"
    }
    return season to year
}
