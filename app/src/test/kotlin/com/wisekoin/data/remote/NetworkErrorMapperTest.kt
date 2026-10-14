package com.wisekoin.data.remote

import java.net.SocketTimeoutException
import okhttp3.Headers.Companion.toHeaders
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkErrorMapperTest {
    @Test
    fun `server details are replaced with a safe message`() {
        val exception = HttpException(
            Response.error<Any>(
                500,
                "{\"error\":\"SQL constraint categories_type_check failed\"}".toResponseBody()
            )
        )

        val error = NetworkErrorMapper.map(exception, "Fallback")

        assertEquals(NetworkErrorKind.SERVER, error.kind)
        assertFalse(error.message.contains("SQL"))
        assertTrue(error.canRetry)
    }

    @Test
    fun `rate limit preserves retry delay without exposing response body`() {
        val exception = HttpException(
            Response.error<Any>(
                "{\"message\":\"provider internals\"}".toResponseBody(),
                okhttp3.Response.Builder()
                    .request(okhttp3.Request.Builder().url("https://example.test/").build())
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(429)
                    .message("Too Many Requests")
                    .headers(mapOf("Retry-After" to "17").toHeaders())
                    .body("".toResponseBody())
                    .build()
            )
        )

        val error = NetworkErrorMapper.map(exception, "Fallback")

        assertEquals(NetworkErrorKind.RATE_LIMITED, error.kind)
        assertEquals(17L, error.retryAfterSeconds)
        assertTrue(error.message.contains("17"))
        assertFalse(error.message.contains("provider"))
    }

    @Test
    fun `timeouts are recoverable`() {
        val error = NetworkErrorMapper.map(SocketTimeoutException(), "Fallback")

        assertEquals(NetworkErrorKind.TIMEOUT, error.kind)
        assertTrue(error.canRetry)
    }

    @Test
    fun `otp error exposes only server approved attempt guidance`() {
        val exception = HttpException(
            Response.error<Any>(
                400,
                "{\"code\":\"OTP_INVALID\",\"error\":\"Incorrect verification code. 2 attempt(s) remaining.\"}"
                    .toResponseBody()
            )
        )

        val error = NetworkErrorMapper.map(exception, "Fallback")

        assertEquals(NetworkErrorKind.VALIDATION, error.kind)
        assertEquals("Incorrect verification code. 2 attempt(s) remaining.", error.message)
        assertFalse(error.canRetry)
    }

    @Test
    fun `current password error uses fixed safe copy`() {
        val exception = HttpException(
            Response.error<Any>(
                400,
                "{\"code\":\"INVALID_CURRENT_PASSWORD\",\"error\":\"internal\"}"
                    .toResponseBody()
            )
        )

        val error = NetworkErrorMapper.map(exception, "Fallback")

        assertEquals("The current password is incorrect.", error.message)
        assertFalse(error.message.contains("internal"))
    }
}
