package com.ghost.zeku.data.remote.anilist

// commonMain/kotlin/.../AniListConfig.kt
expect object AniListConfig {
    val clientId: String
    val clientSecret: String
    val redirectUri: String
}