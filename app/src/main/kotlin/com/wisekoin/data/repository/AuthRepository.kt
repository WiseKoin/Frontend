package com.wisekoin.data.repository

import com.wisekoin.data.model.AuthResponse
import com.wisekoin.data.model.LoginRequest
import com.wisekoin.data.model.RefreshTokenRequest
import com.wisekoin.data.model.RegisterRequest
import com.wisekoin.data.model.UpdateProfileRequest
import com.wisekoin.data.model.UserResponseDto
import com.wisekoin.data.remote.AuthApiService
import com.wisekoin.data.remote.UserApiService

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun logout(refreshToken: String)
    suspend fun getCurrentUser(): UserResponseDto
    suspend fun updateProfile(request: UpdateProfileRequest): UserResponseDto
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
}
