package com.wisekoin.data.remote

import com.wisekoin.data.model.AuthResponse
import com.wisekoin.data.model.LoginRequest
import com.wisekoin.data.model.RefreshTokenRequest
import com.wisekoin.data.model.RegisterRequest
import com.wisekoin.data.model.PasswordRecoveryRequest
import com.wisekoin.data.model.PasswordRecoveryRequestResponse
import com.wisekoin.data.model.PasswordRecoveryResetRequest
import com.wisekoin.data.model.PasswordRecoveryVerifyRequest
import com.wisekoin.data.model.OtpVerifyResponse
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

    @POST("api/auth/password-recovery/request")
    suspend fun requestPasswordRecovery(
        @Body request: PasswordRecoveryRequest
    ): PasswordRecoveryRequestResponse

    @POST("api/auth/password-recovery/verify")
    suspend fun verifyPasswordRecovery(
        @Body request: PasswordRecoveryVerifyRequest
    ): OtpVerifyResponse

    @POST("api/auth/password-recovery/reset")
    suspend fun resetPassword(@Body request: PasswordRecoveryResetRequest)
}
