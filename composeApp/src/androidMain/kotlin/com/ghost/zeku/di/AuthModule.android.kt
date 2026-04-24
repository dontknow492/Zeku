package com.ghost.zeku.di

import com.ghost.zeku.data.repository.auth.AuthConstants
import com.ghost.zeku.data.repository.auth.AuthRedirectHandler
import com.ghost.zeku.data.repository.auth.AuthRedirectListenerFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual fun getPlatformAuthModule(): Module =  module {
    // Android factory doesn't need parameters here
    // (the actual implementation uses the Singleton object we created)
    single { AuthRedirectListenerFactory() }
    single { AuthRedirectHandler() }

    single(named("mal_redirect")) { AuthConstants.REDIRECT_URI_ANDROID }
    single(named("anilist_redirect")) { AuthConstants.REDIRECT_URI_ANDROID }
}