package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.MonthlyStatsDto
import com.wisekoin.data.model.TransactionResponseDto
import com.wisekoin.data.model.WalletRequest
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.data.remote.NetworkErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WalletUiState(
    val totalBalance: BigDecimal = BigDecimal.ZERO,
    val wallets: List<WalletResponseDto> = emptyList(),
    val recentTransactions: List<TransactionResponseDto> = emptyList(),
    val monthlyStats: List<MonthlyStatsDto> = emptyList(),
    val reportTransactions: List<TransactionResponseDto> = emptyList(),
    val reportPeriod: TransactionReportPeriod = TransactionReportPeriod.ALL_TIME,
    val isReportLoading: Boolean = false,
    val reportError: Boolean = false,
    val isLoading: Boolean = false,
    val isCreatingWallet: Boolean = false,
    val error: String? = null,
    val walletCreationFailed: Boolean = false,
    val currentMonthIncome: BigDecimal = BigDecimal.ZERO,
    val currentMonthExpense: BigDecimal = BigDecimal.ZERO
)

class WalletViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()
    private var reportJob: Job? = null
    private var refreshJob: Job? = null
    private var reportRequestId = 0L
    private val walletCreationGate = SubmissionGate()

    fun refreshAll() {
        startRefresh(showLoading = true)
    }

    fun refresh() {
        startRefresh(showLoading = false)
    }

    private fun startRefresh(showLoading: Boolean) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            if (showLoading) _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    awaitAll(
                        async { fetchWallets() },
                        async { fetchRecentTransactions() },
                        async { fetchMonthlyStats() }
                    )
                }
            } finally {
                if (showLoading) _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadWallets() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                fetchWallets()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun createWallet(
        name: String,
        type: String,
        initialBalance: BigDecimal,
        onSuccess: () -> Unit
    ) {
        if (!walletCreationGate.tryStart()) return
        _uiState.update { it.copy(isCreatingWallet = true, walletCreationFailed = false) }
        viewModelScope.launch {
            try {
                RetrofitClient.walletApi.createWallet(
                    WalletRequest(
                        name = name.trim(),
                        walletType = type,
                        initialBalance = initialBalance
                    )
                )
                val summary = RetrofitClient.walletApi.getWallets()
                _uiState.update {
                    it.copy(
                        totalBalance = summary.totalBalance,
                        wallets = summary.wallets,
                        isCreatingWallet = false,
                        walletCreationFailed = false
                    )
                }
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingWallet = false,
                        walletCreationFailed = true
                    )
                }
            } finally {
                walletCreationGate.finish()
            }
        }
    }

    fun clearWalletCreationError() {
        _uiState.update { it.copy(walletCreationFailed = false) }
    }

    fun loadTransactionReport(period: TransactionReportPeriod) {
        if (_uiState.value.isReportLoading && _uiState.value.reportPeriod == period) return
        reportJob?.cancel()
        val requestId = ++reportRequestId
        val range = period.dateRange(LocalDate.now())
        _uiState.update {
            it.copy(
                reportPeriod = period,
                isReportLoading = true,
                reportError = false,
                reportTransactions = emptyList()
            )
        }
        reportJob = viewModelScope.launch {
            try {
                val transactions = mutableListOf<TransactionResponseDto>()
                var page = 0
                do {
                    val response = RetrofitClient.transactionApi.getRecentTransactions(
                        page = page,
                        size = REPORT_PAGE_SIZE,
                        startDate = range.startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        endDate = range.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    )
                    transactions += response.content
                    page++
                } while (page < response.totalPages)

                if (requestId == reportRequestId) {
                    _uiState.update {
                        it.copy(
                            reportTransactions = transactions,
                            isReportLoading = false,
                            reportError = false
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (requestId == reportRequestId) {
                    _uiState.update {
                        it.copy(isReportLoading = false, reportError = true)
                    }
                }
            }
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
        } catch (error: CancellationException) {
            throw error
        } catch (e: Exception) {
            _uiState.update {
                it.copy(error = NetworkErrorMapper.message(e, "Unable to load wallets. Please try again."))
            }
        }
    }

    private suspend fun fetchRecentTransactions() {
        try {
            val response = RetrofitClient.transactionApi.getRecentTransactions(page = 0, size = 10)
            _uiState.update { it.copy(recentTransactions = response.content) }
        } catch (error: CancellationException) {
            throw error
        } catch (e: Exception) {
            _uiState.update {
                it.copy(error = NetworkErrorMapper.message(e, "Unable to load transactions. Please try again."))
            }
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
        } catch (error: CancellationException) {
            throw error
        } catch (e: Exception) {
            _uiState.update {
                it.copy(error = NetworkErrorMapper.message(e, "Unable to load statistics. Please try again."))
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private companion object {
        const val REPORT_PAGE_SIZE = 100
    }
}
