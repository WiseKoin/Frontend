package com.wisekoin.data.repository

import com.wisekoin.data.model.DebtRepaymentRequest
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.DebtResponse
import com.wisekoin.data.remote.RetrofitClient

interface DebtRepository {
    suspend fun getDebts(status: String? = null): List<DebtResponse>
    suspend fun createDebt(request: DebtRequest): DebtResponse
    suspend fun repayDebt(debtId: Long, request: DebtRepaymentRequest): DebtResponse
    suspend fun updateDebt(debtId: Long, request: DebtRequest): DebtResponse
    suspend fun deleteDebt(debtId: Long)
}

class NetworkDebtRepository : DebtRepository {
    override suspend fun getDebts(status: String?): List<DebtResponse> =
        RetrofitClient.debtApi.getDebts(status)

    override suspend fun createDebt(request: DebtRequest): DebtResponse =
        RetrofitClient.debtApi.createDebt(request)

    override suspend fun repayDebt(debtId: Long, request: DebtRepaymentRequest): DebtResponse =
        RetrofitClient.debtApi.repayDebt(debtId, request)

    override suspend fun updateDebt(debtId: Long, request: DebtRequest): DebtResponse =
        RetrofitClient.debtApi.updateDebt(debtId, request)

    override suspend fun deleteDebt(debtId: Long) {
        RetrofitClient.debtApi.deleteDebt(debtId)
    }
}
