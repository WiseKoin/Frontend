package com.wisekoin.data.remote

import com.wisekoin.data.local.SessionTokenStore
import com.wisekoin.data.model.AuthResponse
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class RefreshTokenAuthenticatorTest {
    @Test
    fun `401 refreshes and atomically rotates both tokens`() {
        val store = FakeTokenStore("old-access", "old-refresh")
        var refreshCalls = 0
        val authenticator = RefreshTokenAuthenticator(store) { refreshToken ->
            refreshCalls++
            assertEquals("old-refresh", refreshToken)
            AuthResponse("new-access", "new-refresh", "Bearer", 900)
        }

        val retried = authenticator.authenticate(null, unauthorizedResponse("old-access"))

        assertEquals("Bearer new-access", retried?.header("Authorization"))
        assertEquals("new-access", store.storedAccessToken)
        assertEquals("new-refresh", store.storedRefreshToken)
        assertEquals(1, refreshCalls)
    }

    @Test
    fun `concurrent stale request reuses token already refreshed by another request`() {
        val store = FakeTokenStore("new-access", "new-refresh")
        val authenticator = RefreshTokenAuthenticator(store) {
            throw AssertionError("Refresh endpoint must not be called for an already rotated token")
        }

        val retried = authenticator.authenticate(null, unauthorizedResponse("old-access"))

        assertEquals("Bearer new-access", retried?.header("Authorization"))
    }

    @Test
    fun `failed refresh clears session and does not retry`() {
        val store = FakeTokenStore("old-access", "expired-refresh")
        val authenticator = RefreshTokenAuthenticator(store) { null }

        val retried = authenticator.authenticate(null, unauthorizedResponse("old-access"))

        assertNull(retried)
        assertFalse(store.hasSession())
    }

    private fun unauthorizedResponse(accessToken: String): Response {
        val request = Request.Builder()
            .url("https://api.example.test/api/users/me")
            .header("Authorization", "Bearer $accessToken")
            .build()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
    }

    private class FakeTokenStore(
        var storedAccessToken: String?,
        var storedRefreshToken: String?
    ) : SessionTokenStore {
        override fun saveTokens(accessToken: String, refreshToken: String) {
            storedAccessToken = accessToken
            storedRefreshToken = refreshToken
        }

        override fun getAccessToken(): String? = storedAccessToken

        override fun getRefreshToken(): String? = storedRefreshToken

        override fun clearTokens() {
            storedAccessToken = null
            storedRefreshToken = null
        }

        override fun hasSession(): Boolean = storedAccessToken != null || storedRefreshToken != null
    }
}
