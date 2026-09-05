package com.wisekoin.data.remote

import com.wisekoin.data.model.WalletSummaryResponseDto
import retrofit2.http.GET

interface WalletApiService {
    @GET("api/wallets")
    suspend fun getWallets(): WalletSummaryResponseDto
}
