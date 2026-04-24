package com.ghost.zeku.di

import com.ghost.zeku.data.remote.KtorAuthInterceptor
import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.jikan.JikanApi
import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.provideHttpClientEngine
import com.ghost.zeku.domain.repository.AuthRepository
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import zeku.composeApp.BuildConfig

val networkModule = module {

    // 1. Provide the HttpClient using the platform-specific engine
    single {
        val client = HttpClient(provideHttpClientEngine()) {

            // 1. CONTENT NEGOTIATION (JSON)
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // Crucial for AniList/MAL adding new fields
                    isLenient = true
                    explicitNulls = false // Don't serialize nulls
                })
            }

            // 2. TIMEOUTS (Don't let the app hang forever on a bad connection)
            install(HttpTimeout) {
                requestTimeoutMillis = 15000L // 15 seconds
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 15000L
            }

            // 3. RETRIES (Crucial for mobile networks)
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3) // Retries on 500, 502, 503, 504
                retryOnExceptionIf { request, cause ->
                    // Retry if the network drops temporarily
                    cause is io.ktor.client.network.sockets.SocketTimeoutException ||
                            cause is java.net.UnknownHostException
                }
                exponentialDelay() // Waits 1s, then 2s, then 4s between retries
            }

            // 4. DEFAULT REQUEST (Applies to every call)
            install(DefaultRequest) {
                // Good practice so APIs know what app is pinging them
                headers.append("User-Agent", "ZekuApp/1.0")
            }

            // 5. LOGGING
            install(Logging) {
                level =
                    if (BuildConfig.IS_DEBUG) LogLevel.ALL else LogLevel.INFO // ALL is usually too noisy for production payloads
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.v(tag = "KtorClient", message = message)
                    }
                }
            }
        }
        KtorAuthInterceptor.install(client) {
            get<AuthRepository>()
        }

        client
    }

    // 2. Provide your AniList Api Client
    single { AniListApi(client = get()) }
    single { MalApi(client = get()) }
    single { JikanApi(client = get()) }
}