package com.ghost.zeku.di

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.jikan.JikanApi
import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.provideHttpClientEngine
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {

    // 1. Provide the HttpClient using the platform-specific engine
    single {
        HttpClient(provideHttpClientEngine()) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // Prevents crashes if AniList adds new fields
                    isLenient = true
                })
            }
//             Route Ktor logs to Napier
            install(Logging) {
                level = LogLevel.ALL// Change to LogLevel.INFO if ALL is too noisy
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.v(tag = "KtorClient", message = message)
                    }
                }
            }
        }
    }

    // 2. Provide your AniList Api Client
    single { AniListApi(client = get()) }
    single { MalApi(client = get()) }
    single { JikanApi(client = get()) }
}