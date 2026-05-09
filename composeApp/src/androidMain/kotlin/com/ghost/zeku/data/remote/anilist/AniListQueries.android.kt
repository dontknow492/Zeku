package com.ghost.zeku.data.remote.anilist

import zeku.composeApp.BuildConfig


// androidMain/kotlin/.../AniListConfig.kt
actual object AniListConfig {
    actual val clientId: String = BuildConfig.AL_ID_ANDROID
    actual val clientSecret: String = BuildConfig.AL_SECRET_ANDROID
    actual val redirectUri: String = BuildConfig.AL_REDIRECT_ANDROID
}