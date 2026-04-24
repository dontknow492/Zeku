package com.ghost.zeku.data.remote

import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.util.*

/**
 * A custom Ktor Plugin that automatically injects Auth tokens based on the request host.
 */
class KtorAuthInterceptor(private val authRepository: AuthRepository) {

    companion object Plugin : HttpClientPlugin<Unit, KtorAuthInterceptor> {
        override val key: AttributeKey<KtorAuthInterceptor> = io.ktor.util.AttributeKey("KtorAuthInterceptor")

        override fun prepare(block: Unit.() -> Unit): KtorAuthInterceptor {
            // We can't inject here directly, so we'll get it from the HttpClient configuration
            throw IllegalStateException("Use install(KtorAuthInterceptor) { authRepository = ... }")
        }

        override fun install(plugin: KtorAuthInterceptor, scope: HttpClient) {
            TODO("Not yet implemented")
        }

        // Custom install method for Koin/Manual injection
        fun install(client: HttpClient, authRepositoryProvider: () -> AuthRepository) {
            client.plugin(HttpSend).intercept { request ->
                val host = request.url.host

                val provider = when {
                    host.contains("anilist.co") -> ProviderType.ANILIST
                    host.contains("myanimelist.net") -> ProviderType.MYANIMELIST
                    else -> null
                }

                if (provider != null) {
                    // This lookup happens only on request, long after DI is finished!
                    val token = authRepositoryProvider().getAccessToken(provider)
                    if (!token.isNullOrBlank()) {
                        request.header("Authorization", "Bearer $token")
                    }
                }

                execute(request)
            }
        }
    }
}