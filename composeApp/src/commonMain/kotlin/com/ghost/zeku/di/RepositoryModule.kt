package com.ghost.zeku.di

import com.ghost.zeku.data.remote.anilist.AniListSource
import com.ghost.zeku.data.remote.mal.MalSource
import com.ghost.zeku.data.repository.MediaRepositoryImpl
import com.ghost.zeku.data.repository.UserRepositoryImpl
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.domain.repository.UserRepository
import org.koin.dsl.module

val repoModule = module {
    single<MediaRepository> {
        MediaRepositoryImpl(
            settings = get(),
            database = get(),
            sources = mapOf(
                ProviderType.ANILIST to get<AniListSource>(),
                ProviderType.MYANIMELIST to get<MalSource>()
            )
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            authRepository = get(),
            userDao = get(),
            userSettings = get(),
            mediaSources = listOf(
                get<MalSource>(),
                get<AniListSource>()
            )
        )
    }
}