package com.wisekoin.data.remote

import com.wisekoin.data.model.WalletRequest
import com.wisekoin.data.model.WalletSummaryResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WalletApiService {
    @GET("api/wallets")
    suspend fun getWallets(): WalletSummaryResponseDto

    @POST("api/wallets")
    suspend fun createWallet(@Body request: WalletRequest): WalletSummaryResponseDto
}
