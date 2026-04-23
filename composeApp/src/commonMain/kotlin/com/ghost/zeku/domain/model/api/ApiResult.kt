package com.ghost.zeku.domain.model.api

// Enhanced sealed interface with more granular success states
sealed interface ApiResult<out T> {
    data class Success<T>(
        val data: T,
        val metadata: ResponseMetadata? = null  // For pagination, rate limits, etc.
    ) : ApiResult<T>

    data class Error(
        val error: ApiError,
        val recoverable: Boolean = false  // Indicates if retry might work
    ) : ApiResult<Nothing>

    data class Loading(
        val progress: Float? = null  // Optional progress (0.0 to 1.0)
    ) : ApiResult<Nothing>

    data class Empty(
        val message: String? = null  // When response is empty but valid
    ) : ApiResult<Nothing>
}

// Enhanced error details
data class ApiError(
    val type: ErrorType,
    val code: Int? = null,
    val message: String,
    val rawError: String? = null,
    val recoverable: Boolean,
    val recoverySuggestion: String? = null,  // User-friendly recovery suggestion
    val retryAfter: Long? = null,            // For rate limiting (seconds to wait)
    val cause: Throwable? = null,            // Original exception for debugging
    val endpoint: String? = null,            // Which endpoint failed
    val requestId: String? = null            // For tracking/logging
)

// Enhanced error types
enum class ErrorType {
    // Network related
    NETWORK_UNAVAILABLE,    // No internet connection
    NETWORK_TIMEOUT,        // Request timeout
    NETWORK_SLOW,          // Slow connection

    // Authentication
    UNAUTHORIZED,          // Token expired/invalid
    FORBIDDEN,            // Authenticated but not allowed
    TOKEN_EXPIRED,        // Specific token expiry
    TOKEN_REVOKED,        // Token was revoked

    // Client errors
    BAD_REQUEST,          // 400 - Malformed request
    NOT_FOUND,            // 404 - Resource not found
    RATE_LIMITED,         // 429 - Too many requests

    // Server errors
    SERVER_ERROR,         // 500 - Internal server error
    SERVER_UNAVAILABLE,   // 503 - Service unavailable
    SERVER_TIMEOUT,       // Gateway timeout

    // Data errors
    PARSE_ERROR,          // Failed to parse response
    VALIDATION_ERROR,     // Data validation failed
    EMPTY_RESPONSE,       // Response was empty

    // Business logic
    RESOURCE_CONFLICT,    // 409 - Conflict with current state
    GONE,                 // 410 - Resource no longer available

    // Unknown
    UNKNOWN
}

// Metadata for successful responses
data class ResponseMetadata(
    val pagination: PaginationInfo? = null,
    val rateLimit: RateLimitInfo? = null,
    val cacheInfo: CacheInfo? = null,
    val responseTime: Long? = null  // in milliseconds
)

data class PaginationInfo(
    val currentPage: Int,
    val lastPage: Int,
    val total: Int,
    val perPage: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean = false
)

data class RateLimitInfo(
    val limit: Int,           // Max requests allowed
    val remaining: Int,       // Remaining requests
    val resetAt: Long,        // Timestamp when limit resets
    val retryAfter: Long? = null  // Seconds to wait if limited
)

data class CacheInfo(
    val fromCache: Boolean,
    val cacheKey: String? = null,
    val cachedAt: Long? = null,
    val expiresAt: Long? = null,
    val stale: Boolean = false
)

// Extension functions for easier handling
fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) block(data)
    return this
}

fun <T> ApiResult<T>.onError(block: (ApiError) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) block(error)
    return this
}

fun <T> ApiResult<T>.onEmpty(block: (String?) -> Unit): ApiResult<T> {
    if (this is ApiResult.Empty) block(message)
    return this
}

fun <T> ApiResult<T>.onLoading(block: (Float?) -> Unit): ApiResult<T> {
    if (this is ApiResult.Loading) block(progress)
    return this
}

// Get data or null (safe access)
fun <T> ApiResult<T>.getDataOrNull(): T? = when (this) {
    is ApiResult.Success -> data
    else -> null
}

// Check if successful
fun <T> ApiResult<T>.isSuccess(): Boolean = this is ApiResult.Success

// Check if error is recoverable
fun <T> ApiResult<T>.isRecoverable(): Boolean = when (this) {
    is ApiResult.Error -> recoverable
    else -> false
}

// Get error message with fallback
fun <T> ApiResult<T>.getErrorMessage(default: String = "An error occurred"): String {
    return when (this) {
        is ApiResult.Error -> error.message
        is ApiResult.Empty -> message ?: default
        else -> default
    }
}

// Extension for mapping success data
fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> {
    return when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(data), metadata)
        is ApiResult.Error -> ApiResult.Error(error, recoverable)
        is ApiResult.Empty -> ApiResult.Empty(message)
        is ApiResult.Loading -> ApiResult.Loading(progress)
    }
}

// Extension for flat mapping
fun <T, R> ApiResult<T>.flatMap(transform: (T) -> ApiResult<R>): ApiResult<R> {
    return when (this) {
        is ApiResult.Success -> transform(data)
        is ApiResult.Error -> ApiResult.Error(error, recoverable)
        is ApiResult.Empty -> ApiResult.Empty(message)
        is ApiResult.Loading -> ApiResult.Loading(progress)
    }
}

// Helper to create errors quickly
object ApiErrorFactory {
    fun networkError(cause: Throwable? = null): ApiError = ApiError(
        type = ErrorType.NETWORK_UNAVAILABLE,
        message = "Network connection error. Please check your internet.",
        cause = cause,
        recoverySuggestion = "Check your internet connection and try again",
        recoverable = true
    )

    fun timeoutError(): ApiError = ApiError(
        type = ErrorType.NETWORK_TIMEOUT,
        message = "Request timed out. The server is taking too long to respond.",
        recoverySuggestion = "Please try again later",
        recoverable = true
    )

    fun unauthorizedError(): ApiError = ApiError(
        type = ErrorType.UNAUTHORIZED,
        code = 401,
        message = "Authentication failed. Please log in again.",
        recoverySuggestion = "Please log in to continue",
        recoverable = false
    )

    fun rateLimited(retryAfter: Long): ApiError = ApiError(
        type = ErrorType.RATE_LIMITED,
        code = 429,
        message = "Too many requests. Please slow down.",
        recoverySuggestion = "Please wait ${retryAfter} seconds before trying again",
        retryAfter = retryAfter,
        recoverable = true
    )

    fun serverError(code: Int? = null): ApiError = ApiError(
        type = ErrorType.SERVER_ERROR,
        code = code,
        message = "Server error occurred. Please try again later.",
        recoverySuggestion = "This issue might be temporary. Please try again in a few minutes",
        recoverable = true
    )

    fun parseError(rawError: String, cause: Throwable? = null): ApiError = ApiError(
        type = ErrorType.PARSE_ERROR,
        message = "Failed to process server response.",
        rawError = rawError,
        cause = cause,
        recoverySuggestion = "Please try again or update the app",
        recoverable = true
    )

    fun notFound(resource: String): ApiError = ApiError(
        type = ErrorType.NOT_FOUND,
        code = 404,
        message = "$resource not found.",
        recoverySuggestion = "Please check the information and try again",
        recoverable = false
    )
}

// Result builder for easy creation
inline fun <T> apiResult(block: () -> T): ApiResult<T> {
    return try {
        val result = block()
        if (result != null) {
            ApiResult.Success(result)
        } else {
            ApiResult.Empty("No data available")
        }
    } catch (e: Exception) {
        when (e) {
            is java.net.UnknownHostException -> ApiResult.Error(ApiErrorFactory.networkError(e))
            is java.net.SocketTimeoutException -> ApiResult.Error(ApiErrorFactory.timeoutError())
            else -> ApiResult.Error(
                ApiError(
                    type = ErrorType.UNKNOWN,
                    message = e.message ?: "Unknown error occurred",
                    cause = e,
                    recoverable = true
                )
            )
        }
    }
}