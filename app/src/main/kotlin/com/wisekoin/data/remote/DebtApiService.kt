package com.wisekoin.data.remote

import com.wisekoin.data.model.DebtRepaymentRequest
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.DebtResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DebtApiService {
    @GET("api/debts")
    suspend fun getDebts(@Query("status") status: String? = null): List<DebtResponse>

    @POST("api/debts")
    suspend fun createDebt(@Body request: DebtRequest): DebtResponse

    @POST("api/debts/{id}/repay")
    suspend fun repayDebt(
        @Path("id") debtId: Long,
        @Body request: DebtRepaymentRequest
    ): DebtResponse
}
