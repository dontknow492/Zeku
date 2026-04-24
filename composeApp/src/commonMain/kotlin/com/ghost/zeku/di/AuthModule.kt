package com.ghost.zeku.di

import com.ghost.zeku.data.repository.AniListAuthRepositoryImpl
import com.ghost.zeku.data.repository.AuthRepositoryImpl
import com.ghost.zeku.data.repository.MalAuthRepositoryImpl
import com.ghost.zeku.domain.repository.AuthRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import zeku.composeApp.BuildConfig

val authModule = module {


    // 3. Inject SECURE settings for Auth Repositories
    single {
        AniListAuthRepositoryImpl(settings = get(named("secureSettings")), clientId = BuildConfig.ANILIST_CLIENT_ID)
    }

    single {
        MalAuthRepositoryImpl(
            settings = get(named("secureSettings")),
            httpClient = get(), // Injected from NetworkModule
            clientId = BuildConfig.MAL_CLIENT_ID
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
}