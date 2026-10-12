package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.DebtRepaymentRequest
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.DebtResponse
import com.wisekoin.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class DebtUiState(
    val debts: List<DebtResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRepaying: Boolean = false,
    val repaymentFailed: Boolean = false
) {
    val totalLent: BigDecimal
        get() = debts.filter { it.type == "LEND" }.sumOf { it.remainingAmount }

    val totalBorrowed: BigDecimal
        get() = debts.filter { it.type == "BORROW" }.sumOf { it.remainingAmount }

    val lentDebts: List<DebtResponse>
        get() = debts.filter { it.type == "LEND" }

    val borrowedDebts: List<DebtResponse>
        get() = debts.filter { it.type == "BORROW" }
}

class DebtViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()

    fun loadDebts(status: String? = null) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val debts = RetrofitClient.debtApi.getDebts(status = status)
                _uiState.update { it.copy(debts = debts, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Không thể tải danh sách nợ"
                    )
                }
            }
        }
    }

    suspend fun createDebt(request: DebtRequest): Long? {
        return try {
            val response = RetrofitClient.debtApi.createDebt(request)
            response.id
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Không thể tạo nợ") }
            null
        }
    }

    suspend fun repayDebt(
        debtId: Long,
        amount: BigDecimal,
        walletId: Long,
        categoryId: Long,
        note: String? = null
    ): Boolean {
        if (_uiState.value.isRepaying) return false
        _uiState.update {
            it.copy(isRepaying = true, errorMessage = null, repaymentFailed = false)
        }
        return try {
            val request = DebtRepaymentRequest(
                amount = amount,
                walletId = walletId,
                categoryId = categoryId,
                note = note
            )
            RetrofitClient.debtApi.repayDebt(debtId, request)
            loadDebts()
            true
        } catch (error: CancellationException) {
            throw error
        } catch (e: Exception) {
            _uiState.update { it.copy(repaymentFailed = true) }
            false
        } finally {
            _uiState.update { it.copy(isRepaying = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, repaymentFailed = false) }
    }
}
