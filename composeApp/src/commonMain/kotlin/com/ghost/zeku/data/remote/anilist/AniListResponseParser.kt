package com.ghost.zeku.data.remote.anilist


import com.ghost.zeku.data.remote.anilist.model.AniListError
import com.ghost.zeku.data.remote.anilist.model.AniListResponse
import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiErrorFactory
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ErrorType
import kotlinx.coroutines.CancellationException

// Create a central API response parser
class AniListResponseParser {

    // Add this to AniListResponseParser
    suspend fun <T, R> safeApiCall(
        apiCall: suspend () -> AniListResponse<T>,
        transform: (T) -> R?
    ): ApiResult<R> {
        return try {
            // 1. Execute the network request
            val response = apiCall()

            // 2. Catch GraphQL-specific errors
            if (!response.errors.isNullOrEmpty()) {
                return handleGraphQLErrors(response.errors)
            }

            // 3. Extract and map the data
            val data = response.data
            if (data != null) {
                val result = transform(data)
                if (result != null) {
                    ApiResult.Success(result)
                } else {
                    // e.g., if data.Media was null
                    ApiResult.Error(ApiErrorFactory.notFound("Requested resource"))
                }
            } else {
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.EMPTY_RESPONSE,
                        message = "No data received from server",
                        recoverable = true
                    )
                )
            }
        } catch (e: Exception) {
            // 4. Let the parser handle Ktor/Network exceptions!
            handleException(e)
        }
    }

    // FIXED: Added generic <R> for the transform return type to ensure strict type safety
    fun <T, R> parse(
        response: AniListResponse<T>?,
        transform: (T) -> R
    ): ApiResult<R> {
        return try {
            // Check for GraphQL errors
            if (response?.errors != null && response.errors.isNotEmpty()) {
                return handleGraphQLErrors(response.errors)
            }

            // Check if data exists
            val data = response?.data
            if (data != null) {
                return ApiResult.Success(transform(data))
            } else {
                return ApiResult.Error(
                    ApiError(
                        type = ErrorType.EMPTY_RESPONSE,
                        message = "No data received from server",
                        recoverable = true
                    )
                )
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    fun <T> parseWithData(
        response: AniListResponse<T>?,
        dataExtractor: () -> T?
    ): ApiResult<T> {
        return try {
            // Check for GraphQL errors
            if (response?.errors != null && response.errors.isNotEmpty()) {
                return handleGraphQLErrors(response.errors)
            }

            // Extract data
            val data = dataExtractor()
            if (data != null) {
                ApiResult.Success(data)
            } else {
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.NOT_FOUND,
                        message = "Requested data not found",
                        recoverable = false
                    )
                )
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    fun handleHttpException(e: io.ktor.client.plugins.ResponseException): ApiResult<Nothing> {
        return when (val statusCode = e.response.status.value) {
            400 -> ApiResult.Error(
                ApiError(
                    type = ErrorType.BAD_REQUEST,
                    code = 400,
                    message = "Invalid request format",
                    recoverySuggestion = "Please try again with valid data",
                    recoverable = false
                )
            )

            401 -> ApiResult.Error(
                ApiErrorFactory.unauthorizedError()
            )

            403 -> ApiResult.Error(
                ApiError(
                    type = ErrorType.FORBIDDEN,
                    code = 403,
                    message = "You don't have permission to access this resource",
                    recoverySuggestion = "Please check your account permissions",
                    recoverable = false
                )
            )

            404 -> ApiResult.Error(
                ApiError(
                    type = ErrorType.NOT_FOUND,
                    code = 404,
                    message = "Resource not found",
                    recoverySuggestion = "The requested resource may not exist",
                    recoverable = false
                )
            )

            429 -> {
                val retryAfter = e.response.headers["Retry-After"]?.toLongOrNull()
                ApiResult.Error(
                    ApiErrorFactory.rateLimited(retryAfter ?: 60)
                )
            }

            500, 502, 503, 504 -> ApiResult.Error(
                ApiErrorFactory.serverError(statusCode)
            )

            else -> ApiResult.Error(
                ApiError(
                    type = ErrorType.UNKNOWN,
                    code = statusCode,
                    message = "HTTP error $statusCode occurred",
                    recoverySuggestion = "Please try again later",
                    recoverable = true
                )
            )
        }
    }

    fun handleGraphQLErrors(errors: List<AniListError>): ApiResult<Nothing> {
        val firstError = errors.firstOrNull()

        return when {
            firstError?.message?.contains("not found", ignoreCase = true) == true -> {
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.NOT_FOUND,
                        code = firstError.status ?: 404,
                        message = firstError.message,
                        recoverySuggestion = "The requested resource does not exist",
                        recoverable = false
                    )
                )
            }

            firstError?.message?.contains("unauthorized", ignoreCase = true) == true ||
                    firstError?.message?.contains("invalid token", ignoreCase = true) == true ||
                    firstError?.message?.contains("authentication", ignoreCase = true) == true -> {
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.UNAUTHORIZED,
                        code = firstError.status ?: 401,
                        message = "Authentication failed. Please log in again.",
                        recoverySuggestion = "Please log out and log back in",
                        recoverable = false
                    )
                )
            }

            firstError?.message?.contains("rate limit", ignoreCase = true) == true ||
                    firstError?.message?.contains("too many", ignoreCase = true) == true -> {
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.RATE_LIMITED,
                        code = firstError.status ?: 429,
                        message = "Too many requests. Please slow down.",
                        recoverySuggestion = "Wait a moment before trying again",
                        recoverable = true
                    )
                )
            }

            firstError?.message?.contains("forbidden", ignoreCase = true) == true ||
                    firstError?.message?.contains("permission", ignoreCase = true) == true -> {
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.FORBIDDEN,
                        code = firstError.status ?: 403,
                        message = firstError.message,
                        recoverySuggestion = "You don't have permission for this action",
                        recoverable = false
                    )
                )
            }

            else -> {
                ApiResult.Error(
                    ApiError(
                        type = ErrorType.UNKNOWN,
                        code = firstError?.status,
                        message = firstError?.message ?: "GraphQL error occurred",
                        rawError = errors.joinToString { it.message },
                        recoverable = true
                    )
                )
            }
        }
    }

    private fun <T> handleException(e: Exception): ApiResult<T> {
        // FIXED: Do not swallow Coroutine cancellations!
        if (e is CancellationException) throw e

        return when (e) {
            is io.ktor.client.plugins.ResponseException -> handleHttpException(e)
            is io.ktor.client.plugins.HttpRequestTimeoutException -> {
                ApiResult.Error(ApiErrorFactory.timeoutError())
            }

            is java.net.UnknownHostException -> {
                ApiResult.Error(ApiErrorFactory.networkError(e))
            }

            is java.net.SocketTimeoutException -> {
                ApiResult.Error(ApiErrorFactory.timeoutError())
            }

            else -> {
                ApiResult.Error(
                    ApiErrorFactory.parseError(
                        rawError = e.message ?: "Unknown error",
                        cause = e
                    )
                )
            }
        }
    }
}