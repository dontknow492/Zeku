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
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import zeku.composeApp.BuildConfig

val networkModule = module {

    // 1. Provide the HttpClient using the platform-specific engine
    single {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            encodeDefaults = false
            prettyPrint = false
            coerceInputValues = true
        }
        val client = HttpClient(provideHttpClientEngine()) {

            // 1. CONTENT NEGOTIATION (JSON)
            install(ContentNegotiation) {
                json(json)
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

                header(HttpHeaders.UserAgent, "ZekuApp/1.0")

                accept(ContentType.Application.Json)

                contentType(ContentType.Application.Json)
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

            // ----------------------------------------------------
            // CACHE CONTROL
            // ----------------------------------------------------

            install(HttpCache)

            HttpResponseValidator {

                validateResponse { response ->

                    val statusCode = response.status.value

                    when (statusCode) {

                        in 300..399 -> {
                            Napier.w(
                                tag = "HTTP",
                                message = "Redirect: ${response.status}"
                            )
                        }

                        in 400..499 -> {
                            Napier.w(
                                tag = "HTTP",
                                message = "Client Error: ${response.status}"
                            )
                        }

                        in 500..599 -> {
                            Napier.e(
                                tag = "HTTP",
                                message = "Server Error: ${response.status}"
                            )
                        }
                    }
                }

                handleResponseExceptionWithRequest { exception, request ->

                    Napier.e(
                        tag = "HTTP",
                        throwable = exception,
                        message = "Request failed: ${request.url}"
                    )
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