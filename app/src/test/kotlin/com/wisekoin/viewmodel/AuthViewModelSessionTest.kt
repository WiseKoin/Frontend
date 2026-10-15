package com.wisekoin.viewmodel

import com.wisekoin.data.local.SessionTokenStore
import com.wisekoin.data.model.AuthResponse
import com.wisekoin.data.model.LoginRequest
import com.wisekoin.data.model.RegisterRequest
import com.wisekoin.data.model.UpdateProfileRequest
import com.wisekoin.data.model.UserResponseDto
import com.wisekoin.data.model.PasswordRecoveryRequestResponse
import com.wisekoin.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class AuthViewModelSessionTest {

    @Test
    fun `missing stored session resolves to explicit guest state`() {
        val viewModel = AuthViewModel(FakeAuthRepository(), FakeTokenStore())

        assertEquals(SessionStatus.GUEST, viewModel.uiState.value.sessionStatus)
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `terminal expiry clears identity and emits navigation version once`() {
        val store = FakeTokenStore()
        val viewModel = AuthViewModel(FakeAuthRepository(), store)
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

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(request: LoginRequest): AuthResponse = unsupported()
        override suspend fun register(request: RegisterRequest): AuthResponse = unsupported()
        override suspend fun logout(refreshToken: String) = Unit
        override suspend fun getCurrentUser(): UserResponseDto = unsupported()
        override suspend fun updateProfile(request: UpdateProfileRequest): UserResponseDto = unsupported()
        override suspend fun updateBaseCurrency(currency: String): UserResponseDto = unsupported()
        override suspend fun requestPasswordRecovery(email: String): PasswordRecoveryRequestResponse = unsupported()
        override suspend fun verifyPasswordRecovery(email: String, code: String): String = unsupported()
        override suspend fun resetPassword(
            email: String,
            newPassword: String,
            verificationToken: String
        ) = Unit

        private fun <T> unsupported(): T = error("Not used by this test")
    }
}
