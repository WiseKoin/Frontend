package com.wisekoin.data.repository

import com.wisekoin.data.model.WalletRequest
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.data.model.WalletSummaryResponseDto
import com.wisekoin.data.remote.RetrofitClient

interface WalletRepository {
    suspend fun getWallets(): WalletSummaryResponseDto
    suspend fun createWallet(request: WalletRequest): WalletResponseDto
    suspend fun deleteWallet(walletId: Long)
}

class NetworkWalletRepository : WalletRepository {
    override suspend fun getWallets(): WalletSummaryResponseDto = RetrofitClient.walletApi.getWallets()

    override suspend fun createWallet(request: WalletRequest): WalletResponseDto =
        RetrofitClient.walletApi.createWallet(request)

    override suspend fun deleteWallet(walletId: Long) {
        RetrofitClient.walletApi.deleteWallet(walletId)
    }
}
