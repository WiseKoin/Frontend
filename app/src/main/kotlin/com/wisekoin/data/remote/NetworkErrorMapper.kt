package com.wisekoin.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

enum class NetworkErrorKind {
    OFFLINE,
    TIMEOUT,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    VALIDATION,
    CONFLICT,
    INSUFFICIENT_BALANCE,
    RATE_LIMITED,
    SERVER,
    UNKNOWN
}

data class UserFacingError(
    val kind: NetworkErrorKind,
    val message: String,
    val canRetry: Boolean,
    val retryAfterSeconds: Long? = null
)

object NetworkErrorMapper {
    fun map(error: Throwable, fallbackMessage: String): UserFacingError {
        if (error is SocketTimeoutException) {
            return UserFacingError(
                NetworkErrorKind.TIMEOUT,
                "The request timed out. Check your connection and try again.",
                canRetry = true
            )
        }
        if (error is UnknownHostException || error is IOException && error !is HttpException) {
            return UserFacingError(
                NetworkErrorKind.OFFLINE,
                "You appear to be offline. Check your connection and try again.",
                canRetry = true
            )
        }

        val httpError = error as? HttpException
            ?: return UserFacingError(NetworkErrorKind.UNKNOWN, fallbackMessage, canRetry = true)
        val response = httpError.response()
        val retryAfter = response?.headers()?.get("Retry-After")?.toLongOrNull()
        val errorResponse = response?.errorBody()?.string()?.let { body ->
            runCatching {
                RetrofitClient.getGson().fromJson(body, ErrorResponse::class.java)
            }.getOrNull()
        }
        val errorCode = errorResponse?.code

        return when {
            errorCode == "INSUFFICIENT_BALANCE" -> UserFacingError(
                NetworkErrorKind.INSUFFICIENT_BALANCE,
                "The selected wallet does not have enough available balance.",
                canRetry = false
            )
            errorCode == "OTP_INVALID" -> UserFacingError(
                NetworkErrorKind.VALIDATION,
                errorResponse.error ?: "The verification code is invalid or expired.",
                canRetry = false
            )
            errorCode == "INVALID_CURRENT_PASSWORD" -> UserFacingError(
                NetworkErrorKind.VALIDATION,
                "The current password is incorrect.",
                canRetry = false
            )
            httpError.code() == 400 || httpError.code() == 422 -> UserFacingError(
                NetworkErrorKind.VALIDATION,
                "Review the information you entered and try again.",
                canRetry = false
            )
            httpError.code() == 401 -> UserFacingError(
                NetworkErrorKind.UNAUTHORIZED,
                "Your session has expired. Sign in again to continue.",
                canRetry = false
            )
            httpError.code() == 403 -> UserFacingError(
                NetworkErrorKind.FORBIDDEN,
                "You don’t have permission to perform this action.",
                canRetry = false
            )
            httpError.code() == 404 -> UserFacingError(
                NetworkErrorKind.NOT_FOUND,
                "The requested item is no longer available.",
                canRetry = false
            )
            httpError.code() == 409 -> UserFacingError(
                NetworkErrorKind.CONFLICT,
                "This change conflicts with newer information. Refresh and try again.",
                canRetry = true
            )
            httpError.code() == 429 -> UserFacingError(
                NetworkErrorKind.RATE_LIMITED,
                if (retryAfter != null) {
                    "Too many requests. Try again in $retryAfter seconds."
                } else {
                    "Too many requests. Wait a moment and try again."
                },
                canRetry = true,
                retryAfterSeconds = retryAfter
            )
            httpError.code() >= 500 -> UserFacingError(
                NetworkErrorKind.SERVER,
                "The service is temporarily unavailable. Please try again.",
                canRetry = true
            )
            else -> UserFacingError(NetworkErrorKind.UNKNOWN, fallbackMessage, canRetry = true)
        }
    }

    fun message(error: Throwable, fallbackMessage: String): String =
        map(error, fallbackMessage).message
}
