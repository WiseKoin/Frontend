package com.wisekoin.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.AiCategoryCandidateDto
import com.wisekoin.data.model.AiDuplicateCandidateDto
import com.wisekoin.data.model.AiFieldAmbiguityDto
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.remote.NetworkErrorMapper
import com.wisekoin.data.repository.ReceiptRepository
import com.wisekoin.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import okhttp3.MultipartBody
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

data class ReceiptUiState(
    val imageUri: Uri? = null,
    val storeName: String = "",
    val amount: String = "",
    val categoryId: Long? = null,
    val category: String = "",
    val currency: String = "VND",
    val date: String = "",
    val transactionDate: String? = null,
    val subtotal: BigDecimal? = null,
    val tax: BigDecimal? = null,
    val discount: BigDecimal? = null,
    val tip: BigDecimal? = null,
    val paymentMethod: String = "UNKNOWN",
    val categoryCandidates: List<AiCategoryCandidateDto> = emptyList(),
    val requiresClarification: Boolean = false,
    val ambiguities: List<AiFieldAmbiguityDto> = emptyList(),
    val duplicateCandidates: List<AiDuplicateCandidateDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class ReceiptViewModel(
    private val receiptRepository: ReceiptRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()
    private val submissionGate = SubmissionGate()

    init {
        resetDateTime()
    }

    fun setImageUri(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun uploadReceipt(uri: Uri, filePart: MultipartBody.Part) {
        resetDateTime()
        _uiState.update {
            it.copy(
                imageUri = uri,
                storeName = "",
                amount = "",
                categoryId = null,
                category = "",
                transactionDate = null,
                subtotal = null,
                tax = null,
                discount = null,
                tip = null,
                paymentMethod = "UNKNOWN",
                categoryCandidates = emptyList(),
                requiresClarification = false,
                ambiguities = emptyList(),
                duplicateCandidates = emptyList(),
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val response = receiptRepository.extractReceipt(filePart)

                val formattedAmount = try {
                    NumberFormat.getNumberInstance(Locale.US).format(response.amount)
                } catch (_: Exception) {
                    response.amount.toPlainString()
                }

                _uiState.update {
                    it.copy(
                        storeName = response.storeName,
                        categoryId = response.categoryId,
                        category = response.category,
                        amount = formattedAmount,
                        currency = response.currency ?: "VND",
                        date = formatOccurredAt(response.occurredAt) ?: it.date,
                        transactionDate = response.occurredAt,
                        subtotal = response.subtotal,
                        tax = response.tax,
                        discount = response.discount,
                        tip = response.tip,
                        paymentMethod = response.paymentMethod ?: "UNKNOWN",
                        categoryCandidates = response.categoryCandidates.orEmpty(),
                        requiresClarification = response.requiresClarification,
                        ambiguities = response.ambiguities.orEmpty(),
                        duplicateCandidates = response.duplicateCandidates.orEmpty(),
                        isLoading = false
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        storeName = "Not available",
                        categoryId = null,
                        category = "Not available",
                        amount = "Not available",
                        transactionDate = null,
                        categoryCandidates = emptyList(),
                        requiresClarification = true,
                        ambiguities = emptyList(),
                        duplicateCandidates = emptyList(),
                        isLoading = false,
                        errorMessage = NetworkErrorMapper.message(
                            e,
                            "We couldn’t read this receipt. Retake the photo and try again."
                        )
                    )
                }
            }
        }
    }

    fun updateStoreName(name: String) {
        _uiState.update { it.copy(storeName = name) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateCategory(categoryId: Long?, category: String) {
        _uiState.update { it.copy(categoryId = categoryId, category = category) }
    }

    fun updateDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    private fun resetDateTime() {
        val now = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        _uiState.update { it.copy(date = "Today,\n$now") }
    }

    private fun formatOccurredAt(occurredAt: String?): String? {
        if (occurredAt.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(occurredAt)
                .format(DateTimeFormatter.ofPattern("MMM d, yyyy\nhh:mm a", Locale.US))
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun confirmAndSave(
        categoryViewModel: CategoryViewModel,
        selectedWalletId: Long?,
        isLoggedIn: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentState = _uiState.value

        if (!isLoggedIn) {
            onFailure("Please log in to save a transaction.")
            return
        }
        if (selectedWalletId == null) {
            onFailure("Please select a wallet.")
            return
        }

        val parsedAmount = currentState.amount.replace(",", "").toBigDecimalOrNull()
        if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
            onFailure("Please enter a valid amount.")
            return
        }

        val selectedCategory = categoryViewModel.uiState.value.categories
            .firstOrNull { it.id == currentState.categoryId && it.type == "EXPENSE" }
        if (selectedCategory == null) {
            onFailure("Please select an expense category.")
            return
        }

        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                transactionRepository.createTransaction(
                    TransactionRequest(
                        walletId = selectedWalletId,
                        categoryId = selectedCategory.id,
                        amount = parsedAmount,
                        type = "EXPENSE",
                        note = currentState.storeName.ifBlank { null },
                        transactionDate = currentState.transactionDate
                            ?: LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                )
                resetState()
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                val errorMsg = NetworkErrorMapper.message(e, "Unable to save this transaction. Please try again.")
                onFailure(errorMsg)
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
                submissionGate.finish()
            }
        }
    }

    fun resetState() {
        _uiState.value = ReceiptUiState()
        resetDateTime()
    }
}
