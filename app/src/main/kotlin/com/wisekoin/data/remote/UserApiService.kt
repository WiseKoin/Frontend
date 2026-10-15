package com.wisekoin.data.remote

import com.wisekoin.data.model.UpdateProfileRequest
import com.wisekoin.data.model.UserResponseDto
import com.wisekoin.data.model.ChangePasswordRequest
import com.wisekoin.data.model.BaseCurrencyUpdateRequest
import com.wisekoin.data.model.DeleteAccountRequest
import com.wisekoin.data.model.OtpRequest
import com.wisekoin.data.model.OtpRequestResponse
import com.wisekoin.data.model.OtpVerifyRequest
import com.wisekoin.data.model.OtpVerifyResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/users/me")
    suspend fun getCurrentUser(): UserResponseDto

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserResponseDto

    @PUT("api/users/me/base-currency")
    suspend fun updateBaseCurrency(@Body request: BaseCurrencyUpdateRequest): UserResponseDto

    @POST("api/users/me/otp/request")
    suspend fun requestOtp(@Body request: OtpRequest): OtpRequestResponse

    @POST("api/users/me/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): OtpVerifyResponse

    @PUT("api/users/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest)

    @DELETE("api/users/me")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest)
}
