package com.wisekoin.data.remote

import com.wisekoin.data.model.WalletRequest
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.data.model.WalletSummaryResponseDto
import com.wisekoin.data.model.WalletUpdateRequest
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path

interface WalletApiService {
    @GET("api/wallets")
    suspend fun getWallets(): WalletSummaryResponseDto

    @POST("api/wallets")
    suspend fun createWallet(@Body request: WalletRequest): WalletResponseDto

    @PATCH("api/wallets/{id}")
    suspend fun updateWallet(
        @Path("id") walletId: Long,
        @Body request: WalletUpdateRequest
    ): WalletResponseDto

    @DELETE("api/wallets/{id}")
    suspend fun deleteWallet(@Path("id") walletId: Long)
}
