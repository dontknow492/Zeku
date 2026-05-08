package com.ghost.zeku.data.repository.auth

import io.github.aakira.napier.Napier
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class DesktopRedirectHandler {

    /**
     * Starts a temporary server and waits for the redirect.
     * Returns the full URL string containing the tokens/code.
     */
    suspend fun listenForCode(): String? {
        Napier.d { "DesktopRedirectHandler: Starting local redirect server on port 8080" }

        val result = CompletableDeferred<String>()

        val server = embeddedServer(Netty, port = 8080) {
            routing {
                get("/") {
                    // Capture the full URL (including query parameters)
                    val fullUrl = call.request.local.uri
                    val queryParams = call.request.queryParameters
                    val method = call.request.httpMethod.value
                    val userAgent = call.request.headers["User-Agent"] ?: "unknown"
                    val remoteHost = call.request.local.remoteHost

                    // --- DETAILED LOGGING ---
                    Napier.i { "DesktopRedirectHandler: Redirect received!" }
                    Napier.d {
                        "DesktopRedirectHandler request details:\n" +
                                "  Method: $method\n" +
                                "  Remote Host: $remoteHost\n" +
                                "  Full URI: $fullUrl\n" +
                                "  Query Params Count: ${queryParams.names().size}"
                    }

                    // Log each query parameter individually for debugging
                    queryParams.names().forEach { name ->
                        val value = queryParams[name] ?: "null"
                        // Security: mask tokens but show structure
                        val displayValue = when {
                            name.contains("token", ignoreCase = true) ||
                                    name.contains("code", ignoreCase = true) ||
                                    name.contains("secret", ignoreCase = true) ||
                                    name.contains("auth", ignoreCase = true) ->
                                value.take(6) + "****" + value.takeLast(4)

                            else -> value
                        }
                        Napier.v { "  Query Param '$name': $displayValue" }
                    }
                    Napier.d { "  User-Agent: $userAgent" }
                    // --- END DETAILED LOGGING ---

                    // Show a nice message to the user in their browser
                    // Include debug info so you can ALSO see it in the browser
                    val htmlResponse = """
                        <html>
                            <head>
                                <style>
                                    * { margin: 0; padding: 0; box-sizing: border-box; }
                                    body {
                                        font-family: 'Segoe UI', Tahoma, Geneva, sans-serif;
                                        background: #0d1117;
                                        color: #c9d1d9;
                                        display: flex;
                                        justify-content: center;
                                        align-items: center;
                                        min-height: 100vh;
                                        padding: 20px;
                                    }
                                    .container {
                                        background: #161b22;
                                        border: 1px solid #30363d;
                                        border-radius: 12px;
                                        padding: 40px;
                                        max-width: 700px;
                                        width: 100%;
                                        box-shadow: 0 8px 24px rgba(0,0,0,0.4);
                                    }
                                    h1 {
                                        color: #58a6ff;
                                        margin-bottom: 8px;
                                        font-size: 24px;
                                    }
                                    .success-icon {
                                        font-size: 48px;
                                        margin-bottom: 16px;
                                    }
                                    .subtitle {
                                        color: #8b949e;
                                        margin-bottom: 24px;
                                        font-size: 14px;
                                    }
                                    .debug-section {
                                        background: #0d1117;
                                        border: 1px solid #30363d;
                                        border-radius: 8px;
                                        padding: 16px;
                                        margin-top: 24px;
                                    }
                                    .debug-title {
                                        color: #f0883e;
                                        font-weight: 600;
                                        margin-bottom: 12px;
                                        font-size: 13px;
                                        text-transform: uppercase;
                                        letter-spacing: 0.5px;
                                    }
                                    .debug-item {
                                        padding: 6px 0;
                                        font-size: 12px;
                                        border-bottom: 1px solid #21262d;
                                        word-break: break-all;
                                        font-family: 'Courier New', monospace;
                                    }
                                    .debug-item:last-child {
                                        border-bottom: none;
                                    }
                                    .debug-label {
                                        color: #8b949e;
                                    }
                                    .debug-value {
                                        color: #7ee787;
                                    }
                                    .debug-value.token {
                                        color: #f0883e;
                                    }
                                    .close-hint {
                                        margin-top: 20px;
                                        color: #484f58;
                                        font-size: 12px;
                                        text-align: center;
                                    }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="success-icon">✅</div>
                                    <h1>Login Successful!</h1>
                                    <p class="subtitle">You can close this window and return to Zeku.</p>
                                    
                                    <div class="debug-section">
                                        <div class="debug-title">🔍 Debug Information</div>
                                        <div class="debug-item">
                                            <span class="debug-label">Method:</span> 
                                            <span class="debug-value">$method</span>
                                        </div>
                                        <div class="debug-item">
                                            <span class="debug-label">Remote Host:</span> 
                                            <span class="debug-value">$remoteHost</span>
                                        </div>
                                        <div class="debug-item">
                                            <span class="debug-label">Full URI:</span> 
                                            <span class="debug-value">$fullUrl</span>
                                        </div>
                                        <div class="debug-item">
                                            <span class="debug-label">Query Params Count:</span> 
                                            <span class="debug-value">${queryParams.names().size}</span>
                                        </div>
                                        ${
                        queryParams.names().joinToString("") { name ->
                            val value = queryParams[name] ?: "null"
                            val displayValue = when {
                                name.contains("token", ignoreCase = true) ||
                                        name.contains("code", ignoreCase = true) ||
                                        name.contains("secret", ignoreCase = true) ||
                                        name.contains("auth", ignoreCase = true) ->
                                    value.take(8) + "..." + value.takeLast(4)

                                else -> value
                            }
                            val valueClass = if (name.contains("token", ignoreCase = true) ||
                                name.contains("code", ignoreCase = true) ||
                                name.contains("secret", ignoreCase = true) ||
                                name.contains("auth", ignoreCase = true)
                            ) "token" else ""
                            """
                                                <div class="debug-item">
                                                    <span class="debug-label">$name:</span> 
                                                    <span class="debug-value $valueClass">$displayValue</span>
                                                </div>
                                                """
                        }
                    }
                                        <div class="debug-item">
                                            <span class="debug-label">User-Agent:</span> 
                                            <span class="debug-value">$userAgent</span>
                                        </div>
                                    </div>
                                    
                                    <p class="close-hint">You may now close this browser tab.</p>
                                </div>
                            </body>
                        </html>
                    """.trimIndent()

                    call.respondText(htmlResponse, ContentType.Text.Html)

                    result.complete(fullUrl)
                }
            }
        }.start(wait = false)

        Napier.d { "DesktopRedirectHandler: Server started, waiting for redirect (timeout: 5 minutes)" }

        // Wait for the result or timeout after 5 minutes
        val capturedUrl = withTimeoutOrNull(300_000.milliseconds) {
            result.await()
        }

        if (capturedUrl != null) {
            Napier.i { "DesktopRedirectHandler: Redirect captured successfully" }
            Napier.d { "DesktopRedirectHandler: Captured URL: ${capturedUrl.take(200)}..." }
        } else {
            Napier.w { "DesktopRedirectHandler: Timed out waiting for redirect after 5 minutes" }
        }

        server.stop(1000, 2000)
        Napier.d { "DesktopRedirectHandler: Server stopped" }
        return capturedUrl
    }
}