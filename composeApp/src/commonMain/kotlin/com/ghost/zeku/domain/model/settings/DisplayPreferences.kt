package com.ghost.zeku.domain.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class DisplayPreferences(
    val category: MediaDisplayPreference = MediaDisplayPreference(),
    val search: MediaDisplayPreference = MediaDisplayPreference()
)