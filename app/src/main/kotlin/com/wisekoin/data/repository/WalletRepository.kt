package com.wisekoin.data.repository

import com.wisekoin.data.model.WalletRequest
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.data.model.WalletSummaryResponseDto
import com.wisekoin.data.remote.WalletApiService

interface WalletRepository {
    suspend fun getWallets(): WalletSummaryResponseDto
    suspend fun createWallet(request: WalletRequest): WalletResponseDto
    suspend fun deleteWallet(walletId: Long)
}

class NetworkWalletRepository(
    private val api: WalletApiService
) : WalletRepository {
    override suspend fun getWallets(): WalletSummaryResponseDto = api.getWallets()

    override suspend fun createWallet(request: WalletRequest): WalletResponseDto =
        api.createWallet(request)

    override suspend fun deleteWallet(walletId: Long) {
        api.deleteWallet(walletId)
    }
}
