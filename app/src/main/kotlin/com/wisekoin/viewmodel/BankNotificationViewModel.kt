package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.local.BankNotificationCandidateStore
import com.wisekoin.data.model.BankNotificationCandidate
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.data.remote.NetworkErrorMapper
import com.wisekoin.data.repository.CategoryRepository
import com.wisekoin.data.repository.TransactionRepository
import com.wisekoin.data.repository.WalletRepository
import com.wisekoin.util.MoneyFormatter
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BankNotificationReviewError {
    INVALID_AMOUNT,
    SELECT_WALLET,
    SELECT_CATEGORY,
    CURRENCY_MISMATCH,
    OPTIONS_LOAD_FAILED,
    SAVE_FAILED
}

data class BankNotificationUiState(
    val trackingEnabled: Boolean = false,
    val candidates: List<BankNotificationCandidate> = emptyList(),
    val selectedCandidate: BankNotificationCandidate? = null,
    val amountText: String = "",
    val note: String = "",
    val baseCurrency: String = "VND",
    val wallets: List<WalletResponseDto> = emptyList(),
    val categories: List<CategoryResponse> = emptyList(),
    val selectedWalletId: Long? = null,
    val selectedCategoryId: Long? = null,
    val isLoadingOptions: Boolean = false,
    val isSubmitting: Boolean = false,
    val reviewError: BankNotificationReviewError? = null,
    val networkErrorMessage: String? = null,
    val saveSuccessVersion: Int = 0
)

class BankNotificationViewModel(
    private val store: BankNotificationCandidateStore,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(BankNotificationUiState())
    val uiState: StateFlow<BankNotificationUiState> = mutableState.asStateFlow()
    private val submissionGate = SubmissionGate()

    init {
        viewModelScope.launch {
            store.state.collect { stored ->
                mutableState.update { current ->
                    val selectedStillExists = stored.candidates.any {
                        it.id == current.selectedCandidate?.id
                    }
                    current.copy(
                        trackingEnabled = stored.trackingEnabled,
                        candidates = stored.candidates,
                        selectedCandidate = current.selectedCandidate.takeIf { selectedStillExists }
                    )
                }
            }
        }
    }

    fun setTrackingEnabled(enabled: Boolean) {
        store.setTrackingEnabled(enabled)
    }

    fun openCandidate(candidateId: String, baseCurrency: String) {
        val candidate = store.state.value.candidates.firstOrNull { it.id == candidateId } ?: return
        mutableState.update {
            it.copy(
                selectedCandidate = candidate,
                amountText = MoneyFormatter.format(
                    candidate.amount,
                    candidate.currency,
                    includeCurrencyCode = false
                ),
                note = "",
                baseCurrency = baseCurrency,
                wallets = emptyList(),
                categories = emptyList(),
                selectedWalletId = null,
                selectedCategoryId = null,
                isLoadingOptions = true,
                reviewError = null,
                networkErrorMessage = null
            )
        }
        viewModelScope.launch {
            try {
                val (wallets, categories) = coroutineScope {
                    val walletRequest = async { walletRepository.getWallets().wallets }
                    val categoryRequest = async {
                        categoryRepository.getCategories(candidate.direction.name)
                    }
                    walletRequest.await() to categoryRequest.await()
                }
                if (mutableState.value.selectedCandidate?.id == candidateId) {
                    mutableState.update {
                        it.copy(
                            wallets = wallets,
                            categories = categories,
                            selectedWalletId = wallets.singleOrNull()?.id,
                            selectedCategoryId = categories.singleOrNull()?.id,
                            isLoadingOptions = false,
                            reviewError = if (!candidate.currency.equals(baseCurrency, ignoreCase = true)) {
                                BankNotificationReviewError.CURRENCY_MISMATCH
                            } else {
                                null
                            }
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update {
                    it.copy(
                        isLoadingOptions = false,
                        reviewError = BankNotificationReviewError.OPTIONS_LOAD_FAILED,
                        networkErrorMessage = NetworkErrorMapper.message(
                            error,
                            "Unable to load wallets and categories. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun closeReview() {
        if (!mutableState.value.isSubmitting) {
            mutableState.update {
                it.copy(
                    selectedCandidate = null,
                    reviewError = null,
                    networkErrorMessage = null
                )
            }
        }
    }

    fun updateAmount(value: String) {
        mutableState.update { it.copy(amountText = value, reviewError = null) }
    }

    fun updateNote(value: String) {
        mutableState.update { it.copy(note = value) }
    }

    fun selectWallet(walletId: Long) {
        mutableState.update { it.copy(selectedWalletId = walletId, reviewError = null) }
    }

    fun selectCategory(categoryId: Long) {
        mutableState.update { it.copy(selectedCategoryId = categoryId, reviewError = null) }
    }

    fun retryOptions() {
        val selected = mutableState.value.selectedCandidate ?: return
        openCandidate(selected.id, mutableState.value.baseCurrency)
    }

    fun discard(candidateId: String) {
        if (mutableState.value.isSubmitting) return
        store.remove(candidateId)
    }

    fun clearAll() {
        if (mutableState.value.isSubmitting) return
        store.clearCandidates()
    }

    fun saveSelected() {
        val current = mutableState.value
        val candidate = current.selectedCandidate ?: return
        val amount = MoneyFormatter.parse(current.amountText, candidate.currency)
        val validationError = BankNotificationReviewValidator.validate(
            candidate = candidate,
            parsedAmount = amount,
            baseCurrency = current.baseCurrency,
            selectedWalletId = current.selectedWalletId,
            ownedWalletIds = current.wallets.mapTo(mutableSetOf()) { it.id },
            selectedCategoryId = current.selectedCategoryId,
            ownedCategoryIds = current.categories.mapTo(mutableSetOf()) { it.id }
        )
        if (validationError != null) {
            mutableState.update { it.copy(reviewError = validationError) }
            return
        }
        if (!submissionGate.tryStart()) return

        mutableState.update { it.copy(isSubmitting = true, reviewError = null, networkErrorMessage = null) }
        viewModelScope.launch {
            try {
                transactionRepository.createTransaction(
                    TransactionRequest(
                        walletId = requireNotNull(current.selectedWalletId),
                        categoryId = requireNotNull(current.selectedCategoryId),
                        amount = requireNotNull(amount),
                        type = candidate.direction.name,
                        note = current.note.trim().takeIf(String::isNotBlank),
                        transactionDate = candidate.occurredAt
                    )
                )
                store.remove(candidate.id)
                mutableState.update {
                    it.copy(
                        selectedCandidate = null,
                        isSubmitting = false,
                        reviewError = null,
                        saveSuccessVersion = it.saveSuccessVersion + 1
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.update {
                    it.copy(
                        isSubmitting = false,
                        reviewError = BankNotificationReviewError.SAVE_FAILED,
                        networkErrorMessage = NetworkErrorMapper.message(
                            error,
                            "Unable to save this transaction. Please try again."
                        )
                    )
                }
            } finally {
                submissionGate.finish()
            }
        }
    }
}

object BankNotificationReviewValidator {
    fun validate(
        candidate: BankNotificationCandidate,
        parsedAmount: BigDecimal?,
        baseCurrency: String,
        selectedWalletId: Long?,
        ownedWalletIds: Set<Long>,
        selectedCategoryId: Long?,
        ownedCategoryIds: Set<Long>
    ): BankNotificationReviewError? = when {
        !candidate.currency.equals(baseCurrency, ignoreCase = true) ->
            BankNotificationReviewError.CURRENCY_MISMATCH
        parsedAmount == null || parsedAmount <= BigDecimal.ZERO ->
            BankNotificationReviewError.INVALID_AMOUNT
        selectedWalletId == null || selectedWalletId !in ownedWalletIds ->
            BankNotificationReviewError.SELECT_WALLET
        selectedCategoryId == null || selectedCategoryId !in ownedCategoryIds ->
            BankNotificationReviewError.SELECT_CATEGORY
        else -> null
    }
}
