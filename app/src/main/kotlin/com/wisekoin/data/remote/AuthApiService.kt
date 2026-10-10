package com.wisekoin.data.remote

import com.wisekoin.data.model.AuthResponse
import com.wisekoin.data.model.LoginRequest
import com.wisekoin.data.model.RefreshTokenRequest
import com.wisekoin.data.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("api/auth/refresh")
    fun refresh(
        @Body request: RefreshTokenRequest
    ): Call<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest
    )
}
