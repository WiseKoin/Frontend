package com.wisekoin.data.remote

import com.wisekoin.data.model.MonthlyStatsDto
import com.wisekoin.data.model.PagedTransactionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionApiService {
    @GET("api/transactions")
    suspend fun getRecentTransactions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedTransactionResponse

    @GET("api/transactions/stats")
    suspend fun getMonthlyStats(@Query("year") year: Int): List<MonthlyStatsDto>
}
