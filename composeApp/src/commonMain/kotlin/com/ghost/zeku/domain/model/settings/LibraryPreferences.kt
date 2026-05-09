package com.ghost.zeku.domain.model.settings

import com.ghost.zeku.domain.model.filter.MediaFilterState
import kotlinx.serialization.Serializable

@Serializable
data class LibraryPreferences(
    val displayPreferences: MediaDisplayPreference = MediaDisplayPreference(),
    val filter: MediaFilterState = MediaFilterState()
)