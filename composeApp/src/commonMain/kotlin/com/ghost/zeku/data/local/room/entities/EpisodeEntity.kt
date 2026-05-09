package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import com.ghost.zeku.domain.model.media.DownloadState
import com.ghost.zeku.domain.model.ProviderType

@Entity(
    tableName = "episodes",
    primaryKeys = ["id", "provider"],
    indices = [
        // Crucial: Makes loading all episodes for a specific anime lightning fast
        Index(value = ["mediaId", "provider"]),
        Index(value = ["number"])
    ]
)
data class EpisodeEntity(
    val id: String, // String because scraper APIs sometimes use hashes or strings
    val mediaId: Int, // The ID of the parent Anime
    val provider: ProviderType,

    // Core Network Data
    val number: Float, // Float because some episodes are 10.5 (recaps/specials)
    val title: String?,
    val description: String?,
    val thumbnail: String?,
    val isFiller: Boolean,

    // ==========================================
    // LOCAL APP STATE (The reason we cache!)
    // ==========================================
    val isWatched: Boolean = false,
    val watchProgressMillis: Long = 0L, // To resume the video where they left off

    val downloadStatus: DownloadState = DownloadState.NONE,
    val localFilePath: String? = null // The path to the file downloaded via aria2c/ffmpeg
)


