package com.ghost.zeku.domain.model.filter

import kotlinx.serialization.Serializable

/**
 * Used by the ViewModel to group the final List<MediaEntity> before passing to Compose.
 */
@Serializable
enum class GroupOption {
    NONE,
    SOURCE,     // Group by AniList, MAL, etc.
    CATEGORY,   // Group by "Watching", "Completed", etc. (Requires Library DB join)
    TYPE,       // Group by Anime, Manga, Novel
    STATUS,     // Group by Releasing, Finished, etc.
    FORMAT      // Group by TV, Movie, OVA, etc.
}