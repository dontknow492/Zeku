package com.ghost.zeku.domain

import com.ghost.zeku.domain.AuthSessionHandler
import io.github.aakira.napier.Napier
import io.ktor.http.ContentType
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Desktop
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// desktopMain
actual fun getAuthHandler(): AuthSessionHandler = DesktopAuthHandler()

// desktopMain
class DesktopAuthHandler : AuthSessionHandler {

    private companion object {
        const val TAG = "DesktopAuth"
        private const val PORT = 8080
    }

    override suspend fun getAuthorizationCode(authUrl: String, path: String): String? =
        suspendCancellableCoroutine { continuation ->

            Napier.d(tag = TAG) { "Starting auth handler on port $PORT, path: $path" }
            Napier.d(tag = TAG) { "Auth URL: $authUrl" }

            var serverStarted = false
            var codeReceived = false

            val server = embeddedServer(Netty, port = PORT) {
                routing {
                    route("/auth") {
                        get(path) {
                            Napier.i(tag = TAG) { "Callback received on path: $path" }

                            val code = call.parameters["code"]
                            Napier.d(tag = TAG) { "Code parameter: ${code?.take(10)}..." }

                            call.respondText(
                                "<html><body><h1>Login Successful</h1><p>You can close this tab now.</p></body></html>",
                                ContentType.Text.Html
                            )
                            Napier.d(tag = TAG) { "Response sent to browser" }

                            if (code != null) {
                                Napier.i(tag = TAG) { "Authorization code received successfully" }
                                codeReceived = true
                                continuation.resume(code)
                            } else {
                                Napier.e(tag = TAG) { "No code parameter in callback" }
                                continuation.resumeWithException(
                                    IllegalStateException("Authorization code not found in callback parameters")
                                )
                            }
                        }
                    }


                    // Log any other requests for debugging
                    route("{...}") {
                        handle {
                            Napier.w(tag = TAG) { "Unexpected request: ${call.request.uri}" }
                        }
                    }
                }
            }.start(wait = false).also {
                Napier.i(tag = TAG) { "Local server started on port $PORT" }
                serverStarted = true
            }

            // Open browser
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI(authUrl))
                    Napier.i(tag = TAG) { "Browser opened with auth URL" }
                } else {
                    Napier.w(tag = TAG) { "Desktop not supported, cannot open browser" }
                }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Failed to open browser" }
                continuation.resumeWithException(e)
                return@suspendCancellableCoroutine
            }

            // Cleanup on cancellation or completion
            continuation.invokeOnCancellation { cause ->
                Napier.d(tag = TAG) {
                    "Auth cancelled. Code received: $codeReceived, Server started: $serverStarted"
                }
                if (cause != null) {
                    Napier.w(tag = TAG, throwable = cause) { "Auth was cancelled" }
                }
                server.stop(500, 1000)
                Napier.i(tag = TAG) { "Server stopped" }
            }
        }
            .also { code ->
                Napier.i(tag = TAG) { "getAuthorizationCode completed with code: ${code?.take(10)}..." }
            }
}