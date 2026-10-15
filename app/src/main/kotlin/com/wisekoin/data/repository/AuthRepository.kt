package com.wisekoin.data.repository

import com.wisekoin.data.model.AuthResponse
import com.wisekoin.data.model.LoginRequest
import com.wisekoin.data.model.RefreshTokenRequest
import com.wisekoin.data.model.RegisterRequest
import com.wisekoin.data.model.UpdateProfileRequest
import com.wisekoin.data.model.UserResponseDto
import com.wisekoin.data.model.BaseCurrencyUpdateRequest
import com.wisekoin.data.model.PasswordRecoveryRequest
import com.wisekoin.data.model.PasswordRecoveryRequestResponse
import com.wisekoin.data.model.PasswordRecoveryResetRequest
import com.wisekoin.data.model.PasswordRecoveryVerifyRequest
import com.wisekoin.data.remote.AuthApiService
import com.wisekoin.data.remote.UserApiService

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun logout(refreshToken: String)
    suspend fun getCurrentUser(): UserResponseDto
    suspend fun updateProfile(request: UpdateProfileRequest): UserResponseDto
    suspend fun updateBaseCurrency(currency: String): UserResponseDto
    suspend fun requestPasswordRecovery(email: String): PasswordRecoveryRequestResponse
    suspend fun verifyPasswordRecovery(email: String, code: String): String
    suspend fun resetPassword(email: String, newPassword: String, verificationToken: String)
}

class NetworkAuthRepository(
    private val authApi: AuthApiService,
    private val userApi: UserApiService
) : AuthRepository {
    override suspend fun login(request: LoginRequest): AuthResponse = authApi.login(request)

    override suspend fun register(request: RegisterRequest): AuthResponse = authApi.register(request)

    override suspend fun logout(refreshToken: String) {
        authApi.logout(RefreshTokenRequest(refreshToken))
    }

    override suspend fun getCurrentUser(): UserResponseDto = userApi.getCurrentUser()

    override suspend fun updateProfile(request: UpdateProfileRequest): UserResponseDto =
        userApi.updateProfile(request)

    override suspend fun updateBaseCurrency(currency: String): UserResponseDto =
        userApi.updateBaseCurrency(BaseCurrencyUpdateRequest(currency))

    override suspend fun requestPasswordRecovery(email: String): PasswordRecoveryRequestResponse =
        authApi.requestPasswordRecovery(PasswordRecoveryRequest(email))

    override suspend fun verifyPasswordRecovery(email: String, code: String): String =
        authApi.verifyPasswordRecovery(PasswordRecoveryVerifyRequest(email, code)).verificationToken

    override suspend fun resetPassword(
        email: String,
        newPassword: String,
        verificationToken: String
    ) = authApi.resetPassword(
        PasswordRecoveryResetRequest(email, newPassword, verificationToken)
    )
}
