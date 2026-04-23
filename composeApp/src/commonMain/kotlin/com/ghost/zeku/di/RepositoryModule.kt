package com.ghost.zeku.di

import com.ghost.zeku.data.remote.anilist.AniListSource
import com.ghost.zeku.data.remote.mal.MalSource
import com.ghost.zeku.data.repository.AniListAuthRepositoryImpl
import com.ghost.zeku.data.repository.MalAuthRepositoryImpl
import com.ghost.zeku.data.repository.MediaRepositoryImpl
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.MediaRepository
import org.koin.dsl.module

val repoModule = module {
    single<AniListAuthRepositoryImpl> {
        AniListAuthRepositoryImpl()
    }
    single<MalAuthRepositoryImpl> {
        MalAuthRepositoryImpl()
    }
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
}