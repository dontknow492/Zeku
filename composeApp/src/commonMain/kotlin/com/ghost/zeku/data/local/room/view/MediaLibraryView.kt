package com.ghost.zeku.data.local.room.view

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity
import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.data.local.room.entities.MediaEntity

// MediaLibraryView.kt
@DatabaseView(
    viewName = "media_library_view",
    value = """
        SELECT 
            media.*,
            -- Library Fields
            library.id AS lib_id,
            library.mediaId AS lib_mediaId,
            library.mediaType AS lib_mediaType,
            library.provider AS lib_provider,
            library.categoryId AS lib_categoryId,
            library.favorite AS lib_favorite,
            library.pinned AS lib_pinned,
            library.hidden AS lib_hidden,
            library.downloaded AS lib_downloaded,
            library.downloadPath AS lib_downloadPath,
            library.fileSize AS lib_fileSize,
            library.customTitle AS lib_customTitle,
            library.customDescription AS lib_customDescription,
            library.notes AS lib_notes,
            library.rating AS lib_rating,
            library.watchCount AS lib_watchCount,
            library.lastWatchedAt AS lib_lastWatchedAt,
            library.tags AS lib_tags,
            library.customThumbnailPath AS lib_customThumbnailPath,
            library.addedAt AS lib_addedAt,
            library.updatedAt AS lib_updatedAt,
            library.syncedAt AS lib_syncedAt,
            library.isSynced AS lib_isSynced,
            -- Category Fields
            category.id AS cat_id,
            category.name AS cat_name,
            category.type AS cat_type,
            category.displayName AS cat_displayName,
            category.description AS cat_description,
            category.color AS cat_color,
            category.icon AS cat_icon,
            category.sort_order AS cat_sortOrder,
            category.is_default AS cat_isDefault,
            category.is_visible AS cat_isVisible,
            category.createdAt AS cat_createdAt,
            category.updatedAt AS cat_updatedAt
        FROM media
        LEFT JOIN library ON media.id = library.mediaId 
            AND media.provider = library.provider
        LEFT JOIN library_categories category ON library.categoryId = category.id
    """
)
data class MediaLibraryView(
    @Embedded
    val media: MediaEntity,

    @Embedded(prefix = "lib_")
    val library: LibraryEntity?,

    @Embedded(prefix = "cat_")
    val category: LibraryCategoryEntity?
)
