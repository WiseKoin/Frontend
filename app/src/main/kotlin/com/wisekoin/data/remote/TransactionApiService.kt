package com.wisekoin.data.remote

import com.wisekoin.data.model.MonthlyStatsDto
import com.wisekoin.data.model.PagedTransactionResponse
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.model.TransactionResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TransactionApiService {
    @GET("api/transactions")
    suspend fun getRecentTransactions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): PagedTransactionResponse

    @GET("api/transactions/stats")
    suspend fun getMonthlyStats(@Query("year") year: Int): List<MonthlyStatsDto>

    @POST("api/transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): TransactionResponseDto
}
