package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.model.WalletRequest
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.data.remote.NetworkErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionEntryUiState(
    val isSubmitting: Boolean = false,
    val wallets: List<WalletResponseDto> = emptyList(),
    val isLoadingWallets: Boolean = false,
    val walletLoadError: String? = null
)

class TransactionEntryViewModel : ViewModel() {
    private val submissionGate = SubmissionGate()
    private val _uiState = MutableStateFlow(TransactionEntryUiState())
    val uiState: StateFlow<TransactionEntryUiState> = _uiState.asStateFlow()

    fun loadWallets() {
        if (_uiState.value.isLoadingWallets) return
        _uiState.update { it.copy(isLoadingWallets = true, walletLoadError = null) }
        viewModelScope.launch {
            try {
                val wallets = RetrofitClient.walletApi.getWallets().wallets
                _uiState.update {
                    it.copy(wallets = wallets, isLoadingWallets = false, walletLoadError = null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingWallets = false,
                        walletLoadError = NetworkErrorMapper.message(
                            error,
                            "Unable to load wallets. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun createTransaction(
        request: TransactionRequest,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        submit(
            action = { RetrofitClient.transactionApi.createTransaction(request) },
            onSuccess = { onSuccess() },
            onFailure = onFailure
        )
    }

    fun createDebt(
        request: DebtRequest,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        submit(
            action = { RetrofitClient.debtApi.createDebt(request) },
            onSuccess = { onSuccess() },
            onFailure = onFailure
        )
    }

    fun createWallet(
        request: WalletRequest,
        onSuccess: (WalletResponseDto) -> Unit,
        onFailure: (String) -> Unit
    ) {
        submit(
            action = { RetrofitClient.walletApi.createWallet(request) },
            onSuccess = { wallet ->
                _uiState.update { state ->
                    state.copy(wallets = (state.wallets + wallet).distinctBy { it.id })
                }
                onSuccess(wallet)
            },
            onFailure = onFailure
        )
    }

    private fun <T> submit(
        action: suspend () -> T,
        onSuccess: (T) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                onSuccess(action())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onFailure(NetworkErrorMapper.message(error, "Unable to save. Please try again."))
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
                submissionGate.finish()
            }
        }
    }
}
