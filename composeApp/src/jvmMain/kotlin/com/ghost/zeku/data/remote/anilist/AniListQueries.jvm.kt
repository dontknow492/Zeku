package com.ghost.zeku.data.remote.anilist

import zeku.composeApp.BuildConfig

// desktopMain/kotlin/.../AniListConfig.kt
actual object AniListConfig {
    actual val clientId: String = BuildConfig.AL_ID_DESKTOP
    actual val clientSecret: String = BuildConfig.AL_SECRET_DESKTOP
    actual val redirectUri: String = BuildConfig.AL_REDIRECT_DESKTOP
}