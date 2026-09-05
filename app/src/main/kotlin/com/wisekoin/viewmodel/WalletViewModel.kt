package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.MonthlyStatsDto
import com.wisekoin.data.model.TransactionResponseDto
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WalletUiState(
    val totalBalance: BigDecimal = BigDecimal.ZERO,
    val wallets: List<WalletResponseDto> = emptyList(),
    val recentTransactions: List<TransactionResponseDto> = emptyList(),
    val monthlyStats: List<MonthlyStatsDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentMonthIncome: BigDecimal = BigDecimal.ZERO,
    val currentMonthExpense: BigDecimal = BigDecimal.ZERO
)

class WalletViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            fetchWallets()
            fetchRecentTransactions()
            fetchMonthlyStats()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            fetchWallets()
            fetchRecentTransactions()
            fetchMonthlyStats()
        }
    }

    private suspend fun fetchWallets() {
        try {
            val summary = RetrofitClient.walletApi.getWallets()
            _uiState.update {
                it.copy(
                    totalBalance = summary.totalBalance,
                    wallets = summary.wallets
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.localizedMessage) }
        }
    }

    private suspend fun fetchRecentTransactions() {
        try {
            val response = RetrofitClient.transactionApi.getRecentTransactions(page = 0, size = 10)
            _uiState.update { it.copy(recentTransactions = response.content) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.localizedMessage) }
        }
    }

    private suspend fun fetchMonthlyStats() {
        try {
            val year = LocalDate.now().year
            val stats = RetrofitClient.transactionApi.getMonthlyStats(year)
            _uiState.update { it.copy(monthlyStats = stats) }

            val currentMonth = LocalDate.now().monthValue
            val currentMonthStats = stats.find { it.month == currentMonth }
            _uiState.update {
                it.copy(
                    currentMonthIncome = currentMonthStats?.totalIncome ?: BigDecimal.ZERO,
                    currentMonthExpense = currentMonthStats?.totalExpense ?: BigDecimal.ZERO
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.localizedMessage) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
