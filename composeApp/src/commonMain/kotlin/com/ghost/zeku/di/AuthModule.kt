package com.ghost.zeku.di

import com.ghost.zeku.data.remote.anilist.AniListConfig
import com.ghost.zeku.data.remote.mal.MalConfig
import com.ghost.zeku.data.repository.auth.AniListAuthRepositoryImpl
import com.ghost.zeku.data.repository.auth.AuthRedirectListenerFactory
import com.ghost.zeku.data.repository.auth.AuthRepositoryImpl
import com.ghost.zeku.data.repository.auth.MalAuthRepositoryImpl
import com.ghost.zeku.domain.AuthSessionHandler
import com.ghost.zeku.domain.getAuthHandler
import com.ghost.zeku.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import zeku.composeApp.BuildConfig

expect fun getPlatformAuthModule(): Module

val authModule = module {

    single {
        get<AuthRedirectListenerFactory>().createListener()

    }


    // 3. Inject SECURE settings for Auth Repositories
    single {
        AniListAuthRepositoryImpl(
            settings = get(named("secureSettings")),
            httpClient = get(), // Injected from NetworkModule
            clientId = AniListConfig.clientId,
            clientSecret = AniListConfig.clientSecret,
            redirectUri = AniListConfig.redirectUri
        )
    }

    single {
        MalAuthRepositoryImpl(
            settings = get(named("secureSettings")),
            httpClient = get(), // Injected from NetworkModule
            clientId = MalConfig.clientId,
            redirectUri = MalConfig.redirectUri
        )
    }

    // 4. Combine the secure providers into the main UI Coordinator
    single<AuthRepository> {
        AuthRepositoryImpl(
            providers = listOf(
                get<AniListAuthRepositoryImpl>(),
                get<MalAuthRepositoryImpl>()
            )
        )
    }

    single<AuthSessionHandler> {
        getAuthHandler()
    }
}
