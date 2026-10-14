package com.wisekoin.data.repository

import com.wisekoin.data.model.WalletRequest
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.data.model.WalletSummaryResponseDto
import com.wisekoin.data.model.WalletUpdateRequest
import com.wisekoin.data.remote.WalletApiService
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class WalletRepositoryTest {
    @Test
    fun `network repository delegates to its injected API`() = runBlocking {
        val api = FakeWalletApi()
        val repository = NetworkWalletRepository(api)
        val request = WalletRequest("Savings", "BANK", BigDecimal("250000"))

        assertEquals(BigDecimal("100000"), repository.getWallets().totalBalance)
        assertEquals("Savings", repository.createWallet(request).name)
        repository.deleteWallet(7L)

        assertEquals(request, api.createdRequest)
        assertEquals(7L, api.deletedWalletId)
    }

    private class FakeWalletApi : WalletApiService {
        var createdRequest: WalletRequest? = null
        var deletedWalletId: Long? = null

        override suspend fun getWallets(): WalletSummaryResponseDto =
            WalletSummaryResponseDto(
                totalBalance = BigDecimal("100000"),
                wallets = listOf(WalletResponseDto(1L, "Cash", "CASH", BigDecimal("100000")))
            )

        override suspend fun createWallet(request: WalletRequest): WalletResponseDto {
            createdRequest = request
            return WalletResponseDto(2L, request.name, request.walletType, request.initialBalance)
        }

        override suspend fun updateWallet(
            walletId: Long,
            request: WalletUpdateRequest
        ): WalletResponseDto = error("Not used by this test")

        override suspend fun deleteWallet(walletId: Long) {
            deletedWalletId = walletId
        }
    }
}
