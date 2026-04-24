package com.ghost.zeku.di

import com.ghost.zeku.data.repository.auth.AuthConstants
import com.ghost.zeku.data.repository.auth.AuthRedirectHandler
import com.ghost.zeku.data.repository.auth.AuthRedirectListenerFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual fun getPlatformAuthModule(): Module = module {
    single { AuthRedirectListenerFactory() }
    single { AuthRedirectHandler() }

    single(named("mal_redirect")) { AuthConstants.REDIRECT_URI_JVM }
    single(named("anilist_redirect")) { AuthConstants.REDIRECT_URI_JVM }
}