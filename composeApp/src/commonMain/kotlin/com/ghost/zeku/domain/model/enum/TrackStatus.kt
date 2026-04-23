package com.ghost.zeku.domain.model.enum

import androidx.compose.ui.graphics.Color

// A universal enum that all APIs (MAL, AniList, Kitsu) will be mapped to
enum class TrackStatus(val displayName: String, val color: Color) {
    CURRENT("Watching/Reading", Color(0xFF4CAF50)),
    PLANNING("Plan to Watch/Read", Color(0xFF9E9E9E)),
    COMPLETED("Completed", Color(0xFF2196F3)),
    DROPPED("Dropped", Color(0xFFF44336)),
    PAUSED("Paused", Color(0xFFFFC107)),
    REPEATING("Repeating", Color(0xFF9C27B0)),
    UNKNOWN("Unknown", Color(0xFF607D8B))
}


