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

    //
    SERVICE_UNAVAILABLE,
    UNSUPPORTED_FEATURE,
    CONFLICT,

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
        message = "Unable to reach the server. Please check your internet connection and try again.",
        recoverySuggestion = "Check your internet connection and try again. If you're on mobile data, try switching to Wi-Fi.",
        cause = cause,
        recoverable = true
    )

    fun timeoutError(endpoint: String? = null, timeoutSeconds: Long = 30): ApiError = ApiError(
        type = ErrorType.NETWORK_TIMEOUT,
        message = buildString {
            append("The request took too long to complete")
            if (endpoint != null) append(" while connecting to $endpoint")
            append(". The server may be experiencing high traffic or temporary issues.")
        },
        recoverySuggestion = buildString {
            append("Please try again in a few minutes. ")
            append("If this persists, the service might be temporarily unavailable ")
            append("or undergoing maintenance.")
        },
        recoverable = true
    )

    fun unauthorizedError(provider: String? = null, details: String? = null): ApiError = ApiError(
        type = ErrorType.UNAUTHORIZED,
        code = 401,
        message = buildString {
            append("Your session has expired or your credentials are invalid")
            if (provider != null) append(" for $provider")
            append(". Please log in again to continue.")
            if (details != null) append("\nDetails: $details")
        },
        recoverySuggestion = buildString {
            if (provider != null) {
                append("Please log in to $provider again. ")
                append("If you recently changed your password, you'll need to re-authenticate.")
            } else {
                append("Please log in to continue. Your session may have expired due to inactivity.")
            }
        },
        recoverable = false
    )

    fun rateLimited(
        retryAfter: Long? = null,
        limit: Int? = null,
        window: String? = null
    ): ApiError = ApiError(
        type = ErrorType.RATE_LIMITED,
        code = 429,
        message = buildString {
            append("You've made too many requests in a short period. ")
            if (limit != null) {
                append("The limit is $limit requests")
                if (window != null) append(" per $window")
                append(". ")
            }
            if (retryAfter != null) {
                append("Please wait ${retryAfter} seconds before making another request.")
            } else {
                append("Please wait a moment before trying again.")
            }
        },
        recoverySuggestion = buildString {
            append("This is a protective measure to ensure fair usage for everyone. ")
            if (retryAfter != null) {
                append("You can try again in ${retryAfter} seconds. ")
                val minutes = retryAfter / 60
                if (minutes > 0) {
                    append("(approximately ${minutes} minute${if (minutes > 1) "s" else ""})")
                }
            } else {
                append("Please wait a minute before trying again.")
            }
            append("\nTip: Try reducing the frequency of your requests.")
        },
        retryAfter = retryAfter,
        recoverable = true
    )

    fun serverError(
        code: Int? = null,
        provider: String? = null,
        details: String? = null
    ): ApiError = ApiError(
        type = ErrorType.SERVER_ERROR,
        code = code,
        message = buildString {
            append("The server encountered an unexpected error")
            if (provider != null) append(" on $provider")
            if (code != null) append(" (HTTP $code)")
            append(". This is not your fault — our team has been notified.")
            if (details != null) append("\nTechnical details: $details")
        },
        recoverySuggestion = buildString {
            append("This issue is usually temporary and resolved within a few minutes. ")
            append("Please try again later. If the problem persists for more than ")
            append("15-20 minutes, consider checking our status page or social media ")
            append("for any announced outages.")
        },
        recoverable = true
    )

    fun parseError(
        rawError: String? = null,
        expectedFormat: String? = null,
        cause: Throwable? = null
    ): ApiError = ApiError(
        type = ErrorType.PARSE_ERROR,
        message = buildString {
            append("Failed to process the server's response. ")
            if (expectedFormat != null) {
                append("Expected data in $expectedFormat format, but received something different. ")
            }
            append("This might be due to an API change or corrupted data.")
        },
        rawError = rawError,
        cause = cause,
        recoverySuggestion = buildString {
            append("Please try again. If this keeps happening, ")
            append("the app might need an update. ")
            append("Check if there's a newer version available or contact support ")
            append("if the issue persists.")
        },
        recoverable = true
    )

    fun notFound(
        resource: String,
        id: Any? = null,
        suggestion: String? = null
    ): ApiError = ApiError(
        type = ErrorType.NOT_FOUND,
        code = 404,
        message = buildString {
            append("The requested $resource")
            if (id != null) append(" (ID: $id)")
            append(" could not be found.")
            append(" It may have been removed, made private, or never existed.")
        },
        recoverySuggestion = suggestion ?: buildString {
            append("Please check the ${resource.lowercase()} ID or name and try again. ")
            append("This content might have been removed or is no longer available.")
        },
        recoverable = false
    )

    fun validationError(
        field: String,
        reason: String,
        value: Any? = null
    ): ApiError = ApiError(
        type = ErrorType.BAD_REQUEST,
        code = 400,
        message = buildString {
            append("Invalid input for '$field': $reason")
            if (value != null) append(" (provided: '$value')")
            append(".")
        },
        recoverySuggestion = "Please check your input and try again with valid information.",
        recoverable = true
    )

    fun permissionDenied(
        action: String,
        reason: String? = null
    ): ApiError = ApiError(
        type = ErrorType.UNAUTHORIZED,
        code = 403,
        message = buildString {
            append("You don't have permission to $action")
            if (reason != null) append(". Reason: $reason")
            append(". This action requires additional privileges.")
        },
        recoverySuggestion = buildString {
            append("Check your account permissions. ")
            if (reason == "private") {
                append("This content is private and can only be accessed by its owner.")
            } else {
                append("You might need to upgrade your account or request access.")
            }
        },
        recoverable = false
    )

    fun maintenanceError(
        provider: String,
        estimatedDowntime: String? = null
    ): ApiError = ApiError(
        type = ErrorType.SERVICE_UNAVAILABLE,
        code = 503,
        message = buildString {
            append("$provider is currently undergoing scheduled maintenance. ")
            if (estimatedDowntime != null) {
                append("Expected to be back online within $estimatedDowntime.")
            } else {
                append("The service should be back online shortly.")
            }
        },
        recoverySuggestion = buildString {
            append("This is a temporary maintenance period. ")
            append("Please check back later. ")
            if (estimatedDowntime != null) {
                append("Estimated completion time: $estimatedDowntime.")
            }
        },
        recoverable = true
    )

    fun dataConflict(
        resource: String,
        details: String? = null
    ): ApiError = ApiError(
        type = ErrorType.CONFLICT,
        code = 409,
        message = buildString {
            append("A conflict occurred while updating $resource. ")
            if (details != null) append(details)
            append(" The data may have been modified by another session.")
        },
        recoverySuggestion = buildString {
            append("Please refresh the data and try your changes again. ")
            append("This prevents accidentally overwriting someone else's updates.")
        },
        recoverable = true
    )

    fun mediaNotAvailable(
        mediaType: String,
        title: String,
        reason: String? = null
    ): ApiError = ApiError(
        type = ErrorType.NOT_FOUND,
        code = 404,
        message = buildString {
            append("'$title' ($mediaType) is not available")
            if (reason != null) append(". $reason")
            append(". This content might be region-locked, removed, or not yet released.")
        },
        recoverySuggestion = buildString {
            append("Try searching for similar ${mediaType}s or check back later. ")
            append("Some content becomes available based on your region or subscription status.")
        },
        recoverable = false
    )

    fun providerNotSupported(provider: String, feature: String): ApiError = ApiError(
        type = ErrorType.UNSUPPORTED_FEATURE,
        message = "$provider does not support $feature.",
        recoverySuggestion = "Try using a different provider or feature that is supported by $provider.",
        recoverable = false
    )

    fun sessionExpired(
        provider: String,
        lastActive: String? = null
    ): ApiError = ApiError(
        type = ErrorType.UNAUTHORIZED,
        code = 401,
        message = buildString {
            append("Your $provider session has expired. ")
            if (lastActive != null) append("Last active: $lastActive. ")
            append("For security reasons, sessions are limited in duration.")
        },
        recoverySuggestion = "Please log in again to continue using $provider services.",
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