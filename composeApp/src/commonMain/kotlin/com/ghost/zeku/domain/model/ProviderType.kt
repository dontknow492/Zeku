package com.ghost.zeku.domain.model

import kotlinx.serialization.Serializable

@Serializable
// Standardized types for switching
enum class ProviderType(val authPath: String) {
    MYANIMELIST("/mal"),
    ANILIST("/anilist");

    // Optional: A handy helper to get the enum from a URL path later
    companion object {
        fun fromPath(path: String?): ProviderType? {
            return entries.find { it.authPath == path }
        }
    }
}