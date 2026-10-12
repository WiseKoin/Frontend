package com.wisekoin.viewmodel

import com.wisekoin.data.local.SessionTokenStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class AuthViewModelSessionTest {

    @Test
    fun `missing stored session resolves to explicit guest state`() {
        val viewModel = AuthViewModel()

        viewModel.setTokenManager(FakeTokenStore())

        assertEquals(SessionStatus.GUEST, viewModel.uiState.value.sessionStatus)
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `terminal expiry clears identity and emits navigation version once`() {
        val store = FakeTokenStore()
        val viewModel = AuthViewModel()

        viewModel.setTokenManager(store)
        store.saveTokens("access", "refresh")
        viewModel.onSessionExpired()
        viewModel.onSessionExpired()

        assertFalse(store.hasSession())
        assertEquals(SessionStatus.GUEST, viewModel.uiState.value.sessionStatus)
        assertEquals(1, viewModel.uiState.value.sessionExpiryVersion)
        assertNull(viewModel.uiState.value.currentUser)
    }

    private class FakeTokenStore(
        private var accessToken: String? = null,
        private var refreshToken: String? = null
    ) : SessionTokenStore {
        override fun saveTokens(accessToken: String, refreshToken: String) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
        }

        override fun getAccessToken(): String? = accessToken

        override fun getRefreshToken(): String? = refreshToken

        override fun clearTokens() {
            accessToken = null
            refreshToken = null
        }

        override fun hasSession(): Boolean = accessToken != null && refreshToken != null
    }
}
