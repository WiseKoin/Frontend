package com.wisekoin.data.repository

import com.wisekoin.data.model.MonthlyStatsDto
import com.wisekoin.data.model.PagedTransactionResponse
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.model.TransactionResponseDto
import com.wisekoin.data.remote.TransactionApiService

interface TransactionRepository {
    suspend fun getTransactions(
        page: Int = 0,
        size: Int = 10,
        startDate: String? = null,
        endDate: String? = null
    ): PagedTransactionResponse

    suspend fun getMonthlyStats(year: Int): List<MonthlyStatsDto>
    suspend fun createTransaction(request: TransactionRequest): TransactionResponseDto
}

class NetworkTransactionRepository(
    private val api: TransactionApiService
) : TransactionRepository {
    override suspend fun getTransactions(
        page: Int,
        size: Int,
        startDate: String?,
        endDate: String?
    ): PagedTransactionResponse = api.getRecentTransactions(page, size, startDate, endDate)

    override suspend fun getMonthlyStats(year: Int): List<MonthlyStatsDto> =
        api.getMonthlyStats(year)

    override suspend fun createTransaction(request: TransactionRequest): TransactionResponseDto =
        api.createTransaction(request)
}
