package com.ghost.zeku.data.remote.anilist

import com.ghost.zeku.data.remote.anilist.model.AniListError
import com.ghost.zeku.data.remote.anilist.model.AniListResponse
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiErrorFactory
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ErrorType
import io.github.aakira.napier.Napier
import io.ktor.client.statement.*
import io.ktor.serialization.*
import kotlinx.coroutines.CancellationException

class AniListResponseParser {

    /**
     * Execute an API call safely with full error handling and logging.
     * This is the primary entry point for all AniList API calls.
     */
    suspend fun <T, R> safeApiCall(
        endpoint: String = "unknown",
        apiCall: suspend () -> AniListResponse<T>,
        transform: (T) -> R?
    ): ApiResult<R> {
        Napier.v { "AniList API call started: endpoint=$endpoint" }

        return try {
            val response = apiCall()

            // Log response summary
            Napier.v {
                "AniList response received: endpoint=$endpoint, " +
                        "hasData=${response.data != null}, " +
                        "hasErrors=${response.errors?.isNotEmpty() == true}, " +
                        "errorCount=${response.errors?.size ?: 0}"
            }

            // Handle GraphQL errors first
            if (!response.errors.isNullOrEmpty()) {
                Napier.w {
                    "AniList GraphQL errors: endpoint=$endpoint, " +
                            "errors=${response.errors.joinToString(" | ") { it.message }}"
                }
                return handleGraphQLErrors(endpoint, response.errors)
            }

            // Transform the data
            val data = response.data
            if (data != null) {
                val result = transform(data)
                if (result != null) {
                    Napier.v { "AniList API call successful: endpoint=$endpoint" }
                    ApiResult.Success(result)
                } else {
                    Napier.w { "AniList data transformation returned null: endpoint=$endpoint" }
                    ApiResult.Error(
                        ApiErrorFactory.notFound(
                            resource = "Requested resource",
                            suggestion = "The data could not be extracted from the response"
                        )
                    )
                }
            } else {
                Napier.w { "AniList response had no data and no errors: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.EMPTY_RESPONSE,
                        message = "The server returned an empty response. This might be a temporary issue.",
                        recoverySuggestion = "Please try again in a few moments. If this persists, the data may not be available yet.",
                        recoverable = true,
                        endpoint = endpoint
                    )
                )
            }
        } catch (e: CancellationException) {
            Napier.d { "AniList API call cancelled: endpoint=$endpoint" }
            throw e
        } catch (e: Exception) {
            Napier.e(e) { "AniList API call failed: endpoint=$endpoint" }
            handleException(endpoint, e)
        }
    }


    /**
     * Handle Ktor HTTP response exceptions with detailed error mapping.
     */
    suspend fun handleHttpException(
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
            "AniList HTTP $statusCode: endpoint=$endpoint, body=${responseBody.take(200)}"
        }

        return when (statusCode) {
            400 -> ApiResult.Error(
                ApiErrorFactory.validationError(
                    field = "request",
                    reason = "The request was malformed or contained invalid parameters",
                    value = responseBody.take(100)
                )
            )

            401 -> ApiResult.Error(
                ApiErrorFactory.unauthorizedError(
                    provider = "AniList",
                    details = "HTTP 401 - Your access token may be invalid or expired"
                )
            )

            403 -> ApiResult.Error(
                ApiErrorFactory.permissionDenied(
                    action = "access this resource",
                    reason = "Your account does not have the required permissions. HTTP 403."
                )
            )

            404 -> ApiResult.Error(
                ApiErrorFactory.notFound(
                    resource = "API endpoint or resource",
                    suggestion = "Check that the resource ID is correct. It may have been removed or is not accessible."
                )
            )

            429 -> {
                val retryAfter = e.response.headers["Retry-After"]?.toLongOrNull()
                val rateLimitRemaining = e.response.headers["X-RateLimit-Remaining"]?.toIntOrNull()
                val rateLimitLimit = e.response.headers["X-RateLimit-Limit"]?.toIntOrNull()

                Napier.w {
                    "AniList rate limited: endpoint=$endpoint, " +
                            "retryAfter=$retryAfter, remaining=$rateLimitRemaining, limit=$rateLimitLimit"
                }

                ApiResult.Error(
                    ApiErrorFactory.rateLimited(
                        retryAfter = retryAfter ?: 60,
                        limit = rateLimitLimit ?: 90,
                        window = "minute"
                    )
                )
            }

            500 -> ApiResult.Error(
                ApiErrorFactory.serverError(
                    code = 500,
                    provider = "AniList",
                    details = "Internal server error. The AniList servers are experiencing issues."
                )
            )

            502, 504 -> ApiResult.Error(
                ApiErrorFactory.serverError(
                    code = statusCode,
                    provider = "AniList",
                    details = "Gateway error (HTTP $statusCode). AniList's servers may be overloaded."
                )
            )

            503 -> ApiResult.Error(
                ApiErrorFactory.maintenanceError(
                    provider = "AniList",
                    estimatedDowntime = "a few minutes"
                )
            )

            in 500..599 -> ApiResult.Error(
                ApiErrorFactory.serverError(
                    code = statusCode,
                    provider = "AniList",
                    details = "Server error (HTTP $statusCode). The AniList team has likely been notified."
                )
            )

            else -> ApiResult.Error(
                ApiError(
                    type = ErrorType.UNKNOWN,
                    code = statusCode,
                    message = "An unexpected HTTP error occurred (HTTP $statusCode).",
                    recoverySuggestion = "Please try again later. If this persists, please contact support.",
                    rawError = responseBody.take(200),
                    recoverable = true,
                    endpoint = endpoint
                )
            )
        }
    }

    /**
     * Handle GraphQL-specific errors with context-aware mapping.
     */
    fun handleGraphQLErrors(
        endpoint: String = "unknown",
        errors: List<AniListError>
    ): ApiResult<Nothing> {
        val firstError = errors.firstOrNull() ?: return ApiResult.Error(
            ApiError(
                type = ErrorType.UNKNOWN,
                message = "An unknown GraphQL error occurred.",
                recoverySuggestion = "Please try again. Contact support if this persists.",
                recoverable = true,
                endpoint = endpoint
            )
        )

        val errorMessage = firstError.message
        val statusCode = firstError.status

        Napier.w {
            "AniList GraphQL error: endpoint=$endpoint, " +
                    "status=$statusCode, message=$errorMessage, " +
                    "totalErrors=${errors.size}"
        }

        return when {
            // Authentication errors
            errorMessage.contains("unauthorized", ignoreCase = true) ||
                    errorMessage.contains("invalid token", ignoreCase = true) ||
                    errorMessage.contains("authentication", ignoreCase = true) ||
                    errorMessage.contains("access denied", ignoreCase = true) -> {
                val isTokenExpired = errorMessage.contains("expired", ignoreCase = true)
                ApiResult.Error(
                    ApiErrorFactory.sessionExpired(
                        provider = "AniList",
                        lastActive = if (isTokenExpired) "Token has expired" else null
                    )
                )
            }

            // Not found errors
            errorMessage.contains("not found", ignoreCase = true) ||
                    errorMessage.contains("does not exist", ignoreCase = true) -> {
                val resourceType = extractResourceType(errorMessage) ?: "resource"
                ApiResult.Error(
                    ApiErrorFactory.notFound(
                        resource = resourceType,
                        suggestion = "The $resourceType you're looking for might have been removed or is not available."
                    )
                )
            }

            // Rate limiting
            errorMessage.contains("rate limit", ignoreCase = true) ||
                    errorMessage.contains("too many", ignoreCase = true) ||
                    errorMessage.contains("throttled", ignoreCase = true) -> {
                ApiResult.Error(
                    ApiErrorFactory.rateLimited(
                        retryAfter = 60,
                        limit = 90,
                        window = "minute"
                    )
                )
            }

            // Permission errors
            errorMessage.contains("forbidden", ignoreCase = true) ||
                    errorMessage.contains("permission", ignoreCase = true) ||
                    errorMessage.contains("private", ignoreCase = true) -> {
                ApiResult.Error(
                    ApiErrorFactory.permissionDenied(
                        action = "access this content",
                        reason = if (errorMessage.contains(
                                "private",
                                ignoreCase = true
                            )
                        ) "private" else "insufficient permissions"
                    )
                )
            }

            // Validation errors
            errorMessage.contains("invalid", ignoreCase = true) ||
                    errorMessage.contains("validation", ignoreCase = true) ||
                    errorMessage.contains("required", ignoreCase = true) -> {
                ApiResult.Error(
                    ApiErrorFactory.validationError(
                        field = "query",
                        reason = errorMessage
                    )
                )
            }

            // Server errors
            errorMessage.contains("server error", ignoreCase = true) ||
                    errorMessage.contains("internal", ignoreCase = true) -> {
                ApiResult.Error(
                    ApiErrorFactory.serverError(
                        code = statusCode ?: 500,
                        provider = "AniList",
                        details = errorMessage
                    )
                )
            }

            // Default unknown error
            else -> {
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.UNKNOWN,
                        code = statusCode,
                        message = "AniList API error: $errorMessage",
                        rawError = errors.joinToString("\n") { "- ${it.message}" },
                        recoverySuggestion = "Please try again. If this error persists, please report it.",
                        recoverable = true,
                        endpoint = endpoint
                    )
                )
            }
        }
    }

    /**
     * Handle all types of exceptions with proper error mapping.
     */
    private suspend fun <T> handleException(endpoint: String, e: Exception): ApiResult<T> {
        // NEVER swallow cancellation exceptions
        if (e is CancellationException) {
            Napier.d { "Coroutine cancelled during API call: endpoint=$endpoint" }
            throw e
        }

        Napier.e(e) {
            "Exception during AniList API call: endpoint=$endpoint, " +
                    "type=${e::class.simpleName}, message=${e.message}"
        }

        return when (e) {
            // Ktor Response exception (HTTP errors)
            is io.ktor.client.plugins.ResponseException -> {
                handleHttpException(e, endpoint)
            }

            // Timeout errors
            is io.ktor.client.plugins.HttpRequestTimeoutException -> {
                Napier.w { "AniList request timeout: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.timeoutError(
                        endpoint = endpoint,
                        timeoutSeconds = 30
                    )
                )
            }

            // Network errors
            is java.net.UnknownHostException -> {
                Napier.w { "Network unavailable - unknown host: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.networkError(
                        cause = e
                    )
                )
            }

            is java.net.ConnectException -> {
                Napier.w { "Connection refused: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.networkError(
                        cause = e
                    )
                )
            }

            is java.net.SocketTimeoutException -> {
                Napier.w { "Socket timeout: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.timeoutError(
                        endpoint = endpoint,
                        timeoutSeconds = 30
                    )
                )
            }

            // SSL/TLS errors
            is javax.net.ssl.SSLHandshakeException,
            is javax.net.ssl.SSLException -> {
                Napier.e(e) { "SSL/TLS error: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.NETWORK_UNAVAILABLE,
                        message = "A secure connection could not be established. This might be due to network interference.",
                        recoverySuggestion = "Check your network security settings or try using a different network.",
                        cause = e,
                        recoverable = true,
                        endpoint = endpoint
                    )
                )
            }

            // Serialization/Deserialization errors
            is kotlinx.serialization.SerializationException -> {
                Napier.e(e) { "Serialization error: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.parseError(
                        rawError = e.message,
                        expectedFormat = "GraphQL JSON",
                        cause = e
                    )
                )
            }

            // JSON parsing errors
            is JsonConvertException -> {
                Napier.e(e) { "JSON parsing error: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiErrorFactory.parseError(
                        rawError = e.message,
                        expectedFormat = "valid JSON",
                        cause = e
                    )
                )
            }

            // Catch-all for unknown exceptions
            else -> {
                Napier.e(e) { "Unexpected error: endpoint=$endpoint" }
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.UNKNOWN,
                        message = e.message ?: "An unexpected error occurred while communicating with AniList.",
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
     * Attempt to extract resource type from error message for better user feedback.
     */
    private fun extractResourceType(errorMessage: String): String? {
        val patterns = listOf(
            "media" to "(anime|manga|media)",
            "character" to "character",
            "staff" to "staff",
            "studio" to "studio",
            "user" to "user",
            "list" to "list",
            "review" to "review",
            "activity" to "activity",
            "thread" to "thread|forum"
        )

        for ((type, pattern) in patterns) {
            if (Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(errorMessage)) {
                return type
            }
        }

        return null
    }
}