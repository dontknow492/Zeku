package com.ghost.zeku.presentation.viewmodel.detail

import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.model.media.MediaDate
import com.ghost.zeku.domain.model.media.track.TrackEntry
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.ReviewAction
import com.ghost.zeku.presentation.navigation.Destination

interface MediaDetailContract {

    // -------------------------
    // STATE
    // -------------------------
    data class State(
        // Core Identity
        val id: Int = 0,
        val type: MediaType = MediaType.UNKNOWN,
        val source: ProviderType = ProviderType.MYANIMELIST,
        val title: String = "",
        val nativeTitle: String? = null,
        val synonyms: List<String> = emptyList(),
        val countryOfOrigin: String? = null,

        // Visuals & Text
        val coverImage: String = "",
        val bannerImage: String? = null,
        val extraPictures: List<String> = emptyList(),
        val description: String? = null,
        val background: String? = null,

        // Metadata
        val genres: List<String> = emptyList(),
        val tags: List<MediaTag> = emptyList(),
        val status: MediaReleaseStatus = MediaReleaseStatus.UNKNOWN,
        val format: MediaFormat = MediaFormat.UNKNOWN,
        val sourceMaterial: MediaSourceMaterial? = null,
        val isAdult: Boolean = false,

        // Dates & Schedules
        val startDate: MediaDate? = null,
        val endDate: MediaDate? = null,
        val season: MediaSeason? = null,
        val seasonYear: Int? = null,
        val broadcastString: String? = null,

        // Statistics & Rankings
        val averageScore: Double? = null,
        val meanScore: Double? = null,
        val popularity: Int? = null,
        val favourites: Int? = null,
        val rank: Int? = null,

        // Anime Specific
        val totalEpisodes: Int? = null,
        val durationPerEpisode: Int? = null,
        val contentRating: String? = null,
        val nextAiringEpisode: AiringSchedule? = null,
        val studios: List<MediaStudio> = emptyList(),

        // Manga Specific
        val totalChapters: Int? = null,
        val totalVolumes: Int? = null,
        val serializations: List<String> = emptyList(),

        // 🚀 Small Eager Relational Data
        val trailer: MediaTrailer? = null,
        val externalLinks: List<ExternalLink> = emptyList(),
        val characters: List<MediaCharacter> = emptyList(),
        val relations: List<MediaRelation> = emptyList(),
        val staff: List<MediaStaff> = emptyList(),

        // 👤 User Specific Data (Crucial for FABs and List Management)
        val trackEntry: TrackEntry? = null,

        // Screen State
        val isLoading: Boolean = false,
        val error: String? = null
    )

    // -------------------------
    // EVENTS (UI → VM)
    // -------------------------
    sealed interface Event {
        data class Load(
            val id: Int,
            val type: MediaType
        ) : Event

        object Retry : Event

        data class OnMediaAction(val action: MediaAction) : Event
        data class OnReviewAction(val action: ReviewAction) : Event // Assuming you have ReviewAction defined

        // Trailer events
        data class PlayTrailer(val trailerId: String) : Event

        // External link events
        data class OpenExternalLink(val link: ExternalLink) : Event

        // Navigation events
        data class ViewCharacter(val character: MediaCharacter) : Event
        data class ViewRelation(val relation: MediaRelation) : Event
        data class ViewRecommendation(val media: Media) : Event
        data class ViewAllCharacters(val mediaId: Int) : Event
        data class ViewAllRelations(val mediaId: Int) : Event
        data class ViewAllReviews(val mediaId: Int) : Event
        data class ViewAllRecommendations(val mediaId: Int) : Event

        // Action events
        object ToggleWatchlist : Event
        object StartWatching : Event

        // Chat event
        object OpenChat : Event

        // Entity Clicks (Updated to use actual objects instead of Strings)
        data class OnStaffClick(val staff: MediaStaff) : Event
        data class OnStudioClick(val studio: MediaStudio) : Event
        data class OnTagClick(val tag: MediaTag) : Event
        data class OnGenreClick(val genre: String) : Event


    }

    // -------------------------
    // SIDE EFFECTS (one-time)
    // -------------------------
    sealed interface Effect {
        data class Navigate(val destination: Destination) : Effect
        data class ShowMessage(val message: String, val type: MessageType) : Effect
        data class OpenExternalLink(val link: ExternalLink) : Effect
        data class PlayTrailer(val trailerId: String) : Effect
        data class OpenChat(val id: Int) : Effect
    }
}


//sealed class CreditType(
//    val label: String,
//    val icon: ImageVector
//) {
//    data class Artist(val name: String) : CreditType(
//        label = "Artist",
//        icon = Icons.Filled.Brush
//    )
//
//    data class Author(val name: String) : CreditType(
//        label = "Author",
//        icon = Icons.Filled.Person
//    )
//
//    data class Studio(val name: String) : CreditType(
//        label = "Studio",
//        icon = Icons.Filled.Work
//    )
//}
//
//
////fun CreditType.toEvent(): MediaDetailContract.Event = when (this) {
////    is CreditType.Artist -> MediaDetailContract.Event.OnArtistClick(name)
////    is CreditType.Author -> MediaDetailContract.Event.OnAuthorClick(name)
////    is CreditType.Studio -> MediaDetailContract.Event.OnStudioClick(name)
////}


/**
 * Maps Anime domain model to the universal UI State.
 */
fun MediaDetails.toState(trackEntry: TrackEntry? = null): MediaDetailContract.State {
    return MediaDetailContract.State(
        id = id,
        type = mediaType,
        source = source,
        title = title.getDisplayTitle(),
        nativeTitle = title.native,
        synonyms = synonyms,
        countryOfOrigin = countryOfOrigin,
        coverImage = coverImage,
        bannerImage = bannerImage,
        extraPictures = extraPictures,
        description = description,
        background = background,
        genres = genres,
        tags = tags,
        status = status ?: MediaReleaseStatus.UNKNOWN,
        format = format ?: MediaFormat.UNKNOWN,
        sourceMaterial = sourceMaterial,
        isAdult = isAdult,
        startDate = startDate,
        endDate = endDate,
        season = season,
        seasonYear = seasonYear,
        broadcastString = broadcastString,
        averageScore = averageScore,
        meanScore = meanScore,
        popularity = popularity,
        favourites = favourites,
        rank = rank,
        totalEpisodes = totalEpisodes,
        durationPerEpisode = durationPerEpisode,
        contentRating = contentRating,
        nextAiringEpisode = nextAiringEpisode,
        studios = studios,
        totalChapters = totalChapters,
        totalVolumes = totalVolumes,
        serializations = serializations,
        trailer = trailer,
        externalLinks = externalLinks,
        characters = characters,
        relations = relations,
        staff = staff,
        trackEntry = trackEntry,
        isLoading = false,
        error = null
    )
}

