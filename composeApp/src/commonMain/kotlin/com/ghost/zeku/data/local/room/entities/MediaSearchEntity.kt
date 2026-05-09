package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "media_search")
data class MediaSearchEntity(
    // Note: FTS tables don't need a primary key explicitly declared,
    // it automatically uses a hidden 'rowid'

    val mediaId: Int, // Maps back to your real MediaEntity
    val provider: String,
    val title: String,
    val synonyms: String, // You can dump all alternative titles here as a single string!
    val description: String
)