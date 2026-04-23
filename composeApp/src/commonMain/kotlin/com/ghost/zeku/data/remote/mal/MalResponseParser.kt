package com.ghost.zeku.data.remote.mal


import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiErrorFactory
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.ErrorType
import kotlinx.coroutines.CancellationException

class MalResponseParser {

    suspend fun <T, R> safeApiCall(
        apiCall: suspend () -> T,
        transform: (T) -> R?
    ): ApiResult<R> {
        return try {
            // 1. Execute the REST API call
            val response = apiCall()

            // 2. Transform the data to the Domain model
            val data = transform(response)

            if (data != null) {
                ApiResult.Success(data)
            } else {
                ApiResult.Error(ApiErrorFactory.notFound("Requested data"))
            }
        } catch (e: Exception) {
            // 3. Catch all network/HTTP errors safely
            handleException(e)
        }
    }

    private fun <T> handleException(e: Exception): ApiResult<T> {
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
                        rawError = e.message ?: "Unknown MAL error",
                        cause = e
                    )
                )
            }
        }
    }

    private fun handleHttpException(e: io.ktor.client.plugins.ResponseException): ApiResult<Nothing> {
        return when (val statusCode = e.response.status.value) {
            400 -> ApiResult.Error(
                ApiError(
                    ErrorType.BAD_REQUEST,
                    statusCode,
                    "Invalid request format",
                    recoverable = false
                )
            )

            401 -> ApiResult.Error(ApiErrorFactory.unauthorizedError())
            403 -> ApiResult.Error(ApiError(ErrorType.FORBIDDEN, statusCode, "Access denied", recoverable = false))
            404 -> ApiResult.Error(ApiError(ErrorType.NOT_FOUND, statusCode, "Resource not found", recoverable = false))
            429 -> ApiResult.Error(ApiErrorFactory.rateLimited(60))
            500, 502, 503, 504 -> ApiResult.Error(ApiErrorFactory.serverError(statusCode))
            else -> ApiResult.Error(ApiError(ErrorType.UNKNOWN, statusCode, "HTTP $statusCode", recoverable = true))
        }
    }
}