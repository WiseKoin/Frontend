package com.wisekoin.data.repository

import com.wisekoin.data.model.DebtRepaymentRequest
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.DebtResponse
import com.wisekoin.data.remote.DebtApiService

interface DebtRepository {
    suspend fun getDebts(status: String? = null): List<DebtResponse>
    suspend fun createDebt(request: DebtRequest): DebtResponse
    suspend fun repayDebt(debtId: Long, request: DebtRepaymentRequest): DebtResponse
    suspend fun updateDebt(debtId: Long, request: DebtRequest): DebtResponse
    suspend fun deleteDebt(debtId: Long)
}

class NetworkDebtRepository(
    private val api: DebtApiService
) : DebtRepository {
    override suspend fun getDebts(status: String?): List<DebtResponse> =
        api.getDebts(status)

    override suspend fun createDebt(request: DebtRequest): DebtResponse =
        api.createDebt(request)

    override suspend fun repayDebt(debtId: Long, request: DebtRepaymentRequest): DebtResponse =
        api.repayDebt(debtId, request)

    override suspend fun updateDebt(debtId: Long, request: DebtRequest): DebtResponse =
        api.updateDebt(debtId, request)

    override suspend fun deleteDebt(debtId: Long) {
        api.deleteDebt(debtId)
    }
}
