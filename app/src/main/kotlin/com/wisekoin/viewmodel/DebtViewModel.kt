package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.DebtRepaymentRequest
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.DebtResponse
import com.wisekoin.data.remote.NetworkErrorMapper
import com.wisekoin.data.repository.DebtRepository
import com.wisekoin.data.repository.NetworkDebtRepository
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
    val isCreatingDebt: Boolean = false,
    val repaymentFailed: Boolean = false,
    val editingDebtId: Long? = null,
    val deletingDebtId: Long? = null,
    val mutationError: String? = null
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

class DebtViewModel(
    private val debtRepository: DebtRepository = NetworkDebtRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()
    private val debtCreationGate = SubmissionGate()
    private val repaymentGate = SubmissionGate()
    private val mutationGate = SubmissionGate()

    fun loadDebts(status: String? = null) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val debts = debtRepository.getDebts(status = status)
                _uiState.update { it.copy(debts = debts, isLoading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = NetworkErrorMapper.message(e, "Unable to load debts. Please try again.")
                    )
                }
            }
        }
    }

    suspend fun createDebt(request: DebtRequest): Long? {
        if (!debtCreationGate.tryStart()) return null
        _uiState.update { it.copy(isCreatingDebt = true) }
        return try {
            val response = debtRepository.createDebt(request)
            response.id
        } catch (error: CancellationException) {
            throw error
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = NetworkErrorMapper.message(e, "Unable to create the debt. Please try again."))
            }
            null
        } finally {
            _uiState.update { it.copy(isCreatingDebt = false) }
            debtCreationGate.finish()
        }
    }

    suspend fun repayDebt(
        debtId: Long,
        amount: BigDecimal,
        walletId: Long,
        categoryId: Long,
        note: String? = null
    ): Boolean {
        if (!repaymentGate.tryStart()) return false
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
            debtRepository.repayDebt(debtId, request)
            loadDebts()
            true
        } catch (error: CancellationException) {
            throw error
        } catch (e: Exception) {
            _uiState.update { it.copy(repaymentFailed = true) }
            false
        } finally {
            _uiState.update { it.copy(isRepaying = false) }
            repaymentGate.finish()
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null, repaymentFailed = false, mutationError = null)
        }
    }

    suspend fun updateDebt(debtId: Long, request: DebtRequest): Boolean {
        if (!mutationGate.tryStart()) return false
        _uiState.update { it.copy(editingDebtId = debtId, mutationError = null) }
        return try {
            val updated = debtRepository.updateDebt(debtId, request)
            _uiState.update { state ->
                state.copy(debts = state.debts.map { if (it.id == debtId) updated else it })
            }
            true
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    mutationError = NetworkErrorMapper.message(
                        error,
                        "Unable to update this debt. Review the details and try again."
                    )
                )
            }
            false
        } finally {
            _uiState.update { it.copy(editingDebtId = null) }
            mutationGate.finish()
        }
    }

    suspend fun deleteDebt(debtId: Long): Boolean {
        if (!mutationGate.tryStart()) return false
        _uiState.update { it.copy(deletingDebtId = debtId, mutationError = null) }
        return try {
            debtRepository.deleteDebt(debtId)
            _uiState.update { state ->
                state.copy(debts = state.debts.filterNot { it.id == debtId })
            }
            true
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    mutationError = NetworkErrorMapper.message(
                        error,
                        "This debt cannot be deleted because it has repayment transactions."
                    )
                )
            }
            false
        } finally {
            _uiState.update { it.copy(deletingDebtId = null) }
            mutationGate.finish()
        }
    }
}
