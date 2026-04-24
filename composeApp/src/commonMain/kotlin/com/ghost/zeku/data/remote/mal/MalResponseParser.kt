package com.ghost.zeku.data.remote.mal

import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiErrorFactory
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ErrorType
import io.github.aakira.napier.Napier
import io.ktor.client.statement.*
import io.ktor.serialization.*
import kotlinx.coroutines.CancellationException

class MalResponseParser {

    /**
     * Execute a MAL API call safely with full error handling, logging, and transformation.
     * This is the primary entry point for all MyAnimeList API calls.
     */
    suspend fun <T, R> safeApiCall(
        endpoint: String = "unknown",
        apiCall: suspend () -> T,
        transform: (T) -> R?
    ): ApiResult<R> {
        Napier.v { "MAL API call started: endpoint=$endpoint" }

        return try {
            val response = apiCall()
            Napier.v { "MAL response received: endpoint=$endpoint, type=${response?.let { it::class.simpleName } ?: "null"}" }

            val data = transform(response)

            if (data != null) {
                Napier.v { "MAL API call successful: endpoint=$endpoint" }
                ApiResult.Success(data)
            } else {
                Napier.w { "MAL data transformation returned null: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.notFound(
                        resource = "Requested data",
                        suggestion = "The API response was valid but contained no usable data. The resource might not exist or may have been removed."
                    )
                )
            }
        } catch (e: CancellationException) {
            Napier.d { "MAL API call cancelled: endpoint=$endpoint" }
            throw e
        } catch (e: Exception) {
            Napier.e(e) { "MAL API call failed: endpoint=$endpoint" }
            handleException(endpoint, e)
        }
    }

    /**
     * Execute a MAL API call that returns a list/collection.
     * Adds additional handling for empty collections.
     */
    suspend fun <T, R> safeApiListCall(
        endpoint: String = "unknown",
        apiCall: suspend () -> T,
        transform: (T) -> List<R>?
    ): ApiResult<List<R>> {
        Napier.v { "MAL API list call started: endpoint=$endpoint" }

        return try {
            val response = apiCall()
            Napier.v { "MAL list response received: endpoint=$endpoint" }

            val dataList = transform(response)

            when {
                dataList == null -> {
                    Napier.w { "MAL list transformation returned null: endpoint=$endpoint" }
                    ApiResult.Error(
                        ApiErrorFactory.parseError(
                            rawError = "Transform returned null",
                            expectedFormat = "List",
                            cause = null
                        )
                    )
                }

                dataList.isEmpty() -> {
                    Napier.v { "MAL list call returned empty: endpoint=$endpoint" }
                    ApiResult.Empty("No results found for this request.")
                }

                else -> {
                    Napier.v { "MAL list call successful: endpoint=$endpoint, count=${dataList.size}" }
                    ApiResult.Success(dataList)
                }
            }
        } catch (e: CancellationException) {
            Napier.d { "MAL API list call cancelled: endpoint=$endpoint" }
            throw e
        } catch (e: Exception) {
            Napier.e(e) { "MAL API list call failed: endpoint=$endpoint" }
            handleException(endpoint, e)
        }
    }

    /**
     * Handle Ktor HTTP response exceptions with detailed error mapping for MAL's REST API.
     */
    private suspend fun handleHttpException(
        e: io.ktor.client.plugins.ResponseException,
        endpoint: String = "unknown"
    ): ApiResult<Nothing> {
        val statusCode = e.response.status.value
        val responseBody = try {
            e.response.bodyAsText().take(500)
        } catch (ex: Exception) {
            "Unable to read response body"
        }

        Napier.w {
            "MAL HTTP $statusCode: endpoint=$endpoint, " +
                    "statusMessage=${e.response.status.description}, " +
                    "body=${responseBody.take(200)}"
        }

        return when (statusCode) {
            400 -> {
                val isMalformed = responseBody.contains("invalid", ignoreCase = true) ||
                        responseBody.contains("malformed", ignoreCase = true) ||
                        responseBody.contains("bad request", ignoreCase = true)

                if (isMalformed) {
                    ApiResult.Error(
                        ApiErrorFactory.validationError(
                            field = "request",
                            reason = "The request was malformed or contained invalid parameters",
                            value = responseBody.take(100)
                        )
                    )
                } else {
                    ApiResult.Error(
                        ApiError(
                            type = ErrorType.BAD_REQUEST,
                            code = 400,
                            message = "The request was invalid. This might be due to incorrect parameters or formatting.",
                            recoverySuggestion = "Check your request parameters and try again.",
                            rawError = responseBody.take(200),
                            recoverable = false,
                            endpoint = endpoint
                        )
                    )
                }
            }

            401 -> {
                val isTokenExpired = responseBody.contains("expired", ignoreCase = true)
                val isInvalidToken = responseBody.contains("invalid", ignoreCase = true)

                Napier.w {
                    "MAL 401 Unauthorized: endpoint=$endpoint, " +
                            "tokenExpired=$isTokenExpired, invalidToken=$isInvalidToken"
                }

                ApiResult.Error(
                    if (isTokenExpired) {
                        ApiErrorFactory.sessionExpired(
                            provider = "MyAnimeList",
                            lastActive = "Token has expired"
                        )
                    } else {
                        ApiErrorFactory.unauthorizedError(
                            provider = "MyAnimeList",
                            details = if (isInvalidToken) "The access token is invalid" else null
                        )
                    }
                )
            }

            403 -> {
                val isPrivate = responseBody.contains("private", ignoreCase = true)
                ApiResult.Error(
                    ApiErrorFactory.permissionDenied(
                        action = "access this resource on MyAnimeList",
                        reason = if (isPrivate) "private" else "insufficient permissions"
                    )
                )
            }

            404 -> {
                val resourceType = extractMALResourceType(responseBody, endpoint)
                ApiResult.Error(
                    ApiErrorFactory.notFound(
                        resource = resourceType ?: "resource",
                        suggestion = "The requested ${resourceType ?: "content"} could not be found on MyAnimeList. It may have been removed or is not available."
                    )
                )
            }

            429 -> {
                val retryAfter = e.response.headers["Retry-After"]?.toLongOrNull()
                val malRateLimit = e.response.headers["X-RateLimit-Remaining"]?.toIntOrNull()

                Napier.w {
                    "MAL rate limited: endpoint=$endpoint, " +
                            "retryAfter=$retryAfter, remaining=$malRateLimit"
                }

                ApiResult.Error(
                    ApiErrorFactory.rateLimited(
                        retryAfter = retryAfter ?: 60,
                        limit = malRateLimit?.let { it + 1 }, // Approximate limit
                        window = "minute"
                    )
                )
            }

            500 -> {
                Napier.e { "MAL 500 Internal Server Error: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.serverError(
                        code = 500,
                        provider = "MyAnimeList",
                        details = "Internal server error. MAL servers are experiencing issues."
                    )
                )
            }

            502 -> {
                Napier.e { "MAL 502 Bad Gateway: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.serverError(
                        code = 502,
                        provider = "MyAnimeList",
                        details = "MAL's servers are temporarily overloaded or down for maintenance (502 Bad Gateway)."
                    )
                )
            }

            503 -> {
                Napier.w { "MAL 503 Service Unavailable: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.maintenanceError(
                        provider = "MyAnimeList",
                        estimatedDowntime = "a few minutes"
                    )
                )
            }

            504 -> {
                Napier.e { "MAL 504 Gateway Timeout: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.serverError(
                        code = 504,
                        provider = "MyAnimeList",
                        details = "MAL's servers timed out processing your request (504 Gateway Timeout)."
                    )
                )
            }

            in 500..599 -> {
                Napier.e { "MAL ${statusCode} Server Error: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.serverError(
                        code = statusCode,
                        provider = "MyAnimeList",
                        details = "Server error occurred (HTTP $statusCode)."
                    )
                )
            }

            else -> {
                Napier.w { "MAL unexpected HTTP $statusCode: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.UNKNOWN,
                        code = statusCode,
                        message = "An unexpected HTTP error occurred (HTTP $statusCode) while communicating with MyAnimeList.",
                        recoverySuggestion = "Please try again later. If this persists, check MAL's status page.",
                        rawError = responseBody.take(200),
                        recoverable = true,
                        endpoint = endpoint
                    )
                )
            }
        }
    }

    /**
     * Handle all types of exceptions with proper error mapping for MAL's REST API.
     */
    private suspend fun <T> handleException(endpoint: String, e: Exception): ApiResult<T> {
        // NEVER swallow cancellation exceptions
        if (e is CancellationException) {
            Napier.d { "Coroutine cancelled during MAL API call: endpoint=$endpoint" }
            throw e
        }

        Napier.e(e) {
            "Exception during MAL API call: endpoint=$endpoint, " +
                    "type=${e::class.simpleName}, message=${e.message}"
        }

        return when (e) {
            // Ktor Response exception (HTTP errors)
            is io.ktor.client.plugins.ResponseException -> {
                handleHttpException(e, endpoint)
            }

            // Timeout errors
            is io.ktor.client.plugins.HttpRequestTimeoutException -> {
                Napier.w { "MAL request timeout: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.timeoutError(
                        endpoint = endpoint,
                        timeoutSeconds = 30
                    )
                )
            }

            // Network errors
            is java.net.UnknownHostException -> {
                Napier.w { "Network unavailable - unknown host for MAL: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.networkError(
                        cause = e
                    )
                )
            }

            is java.net.ConnectException -> {
                Napier.w { "Connection refused to MAL: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.NETWORK_UNAVAILABLE,
                        message = "Could not connect to MyAnimeList. The service might be temporarily unavailable or blocked.",
                        recoverySuggestion = "Check your internet connection. If you're on a restricted network, MAL might be blocked.",
                        cause = e,
                        recoverable = true,
                        endpoint = endpoint
                    )
                )
            }

            is java.net.SocketTimeoutException -> {
                Napier.w { "Socket timeout connecting to MAL: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.timeoutError(
                        endpoint = endpoint,
                        timeoutSeconds = 30
                    )
                )
            }

            is java.io.IOException -> {
                Napier.w { "IO error during MAL request: endpoint=$endpoint, message=${e.message}" }
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.NETWORK_UNAVAILABLE,
                        message = "A network error occurred while communicating with MyAnimeList.",
                        recoverySuggestion = "Check your internet connection and try again.",
                        cause = e,
                        recoverable = true,
                        endpoint = endpoint
                    )
                )
            }

            // SSL/TLS errors
            is javax.net.ssl.SSLHandshakeException,
            is javax.net.ssl.SSLException -> {
                Napier.e(e) { "SSL/TLS error connecting to MAL: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.NETWORK_UNAVAILABLE,
                        message = "A secure connection to MyAnimeList could not be established. This might be due to network interference or outdated security settings.",
                        recoverySuggestion = "Check your system's date/time settings and try using a different network.",
                        cause = e,
                        recoverable = true,
                        endpoint = endpoint
                    )
                )
            }

            // Serialization/Deserialization errors
            is kotlinx.serialization.SerializationException -> {
                Napier.e(e) { "Serialization error parsing MAL response: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.parseError(
                        rawError = e.message,
                        expectedFormat = "MAL JSON response",
                        cause = e
                    )
                )
            }

            is JsonConvertException -> {
                Napier.e(e) { "JSON parsing error for MAL response: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.parseError(
                        rawError = e.message,
                        expectedFormat = "valid JSON from MAL API",
                        cause = e
                    )
                )
            }

            // OAuth/Token specific errors
            is java.lang.IllegalStateException -> {
                if (e.message?.contains("token", ignoreCase = true) == true ||
                    e.message?.contains("auth", ignoreCase = true) == true ||
                    e.message?.contains("PKCE", ignoreCase = true) == true
                ) {
                    Napier.w { "MAL authentication error: ${e.message}" }
                    ApiResult.Error(
                        ApiErrorFactory.unauthorizedError(
                            provider = "MyAnimeList",
                            details = e.message
                        )
                    )
                } else {
                    Napier.e(e) { "Unexpected illegal state in MAL parser: endpoint=$endpoint" }
                    ApiResult.Error(
                        ApiError(
                            type = ErrorType.UNKNOWN,
                            message = e.message ?: "An unexpected application error occurred.",
                            recoverySuggestion = "Please restart the app and try again.",
                            cause = e,
                            recoverable = true,
                            endpoint = endpoint
                        )
                    )
                }
            }

            // MAL-specific API errors
            is java.lang.IllegalArgumentException -> {
                Napier.w { "MAL invalid argument: endpoint=$endpoint, message=${e.message}" }
                ApiResult.Error(
                    ApiErrorFactory.validationError(
                        field = "request parameters",
                        reason = e.message ?: "Invalid argument provided"
                    )
                )
            }

            // Catch-all for unknown exceptions
            else -> {
                Napier.e(e) { "Unexpected error during MAL API call: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.UNKNOWN,
                        message = "An unexpected error occurred while communicating with MyAnimeList: ${e.message}",
                        recoverySuggestion = "Please try again. If this continues, restart the app or contact support.",
                        cause = e,
                        recoverable = true,
                        endpoint = endpoint
                    )
                )
            }
        }
    }

    /**
     * Attempt to extract resource type from MAL error messages or endpoint for better user feedback.
     */
    private fun extractMALResourceType(responseBody: String, endpoint: String): String? {
        // Try to determine from response body
        val bodyPatterns = listOf(
            "anime" to "anime",
            "manga" to "manga",
            "user" to "user",
            "list" to "list",
            "review" to "review",
            "recommendation" to "recommendation",
            "forum" to "forum",
            "club" to "club",
            "character" to "character",
            "person" to "person",
            "episode" to "episode",
            "chapter" to "chapter"
        )

        for ((type, pattern) in bodyPatterns) {
            if (responseBody.contains(pattern, ignoreCase = true)) {
                return type
            }
        }

        // Fallback: try to extract from endpoint
        val endpointPatterns = listOf(
            "anime" to "anime",
            "manga" to "manga",
            "users" to "user",
            "lists" to "list",
            "reviews" to "review",
            "recommendations" to "recommendation",
            "forum" to "forum",
            "clubs" to "club",
            "characters" to "character",
            "people" to "person"
        )

        for ((path, type) in endpointPatterns) {
            if (endpoint.contains(path, ignoreCase = true)) {
                return type
            }
        }

        return null
    }
}