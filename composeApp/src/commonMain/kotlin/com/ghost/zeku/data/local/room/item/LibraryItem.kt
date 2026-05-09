package com.ghost.zeku.data.local.room.item

import androidx.room.Embedded
import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.data.local.room.entities.MediaEntity

data class LibraryItem(
    @Embedded
    val media: MediaEntity,

    // We add a prefix so Room knows which columns belong to the library
    @Embedded(prefix = "lib_")
    val library: LibraryEntity?
)

