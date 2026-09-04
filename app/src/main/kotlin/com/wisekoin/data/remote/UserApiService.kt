package com.wisekoin.data.remote

import com.wisekoin.data.model.UpdateProfileRequest
import com.wisekoin.data.model.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/users/me")
    suspend fun getCurrentUser(): UserResponseDto

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserResponseDto
}
