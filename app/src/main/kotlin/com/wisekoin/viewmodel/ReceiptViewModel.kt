package com.wisekoin.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.ReceiptResponseDto
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.data.remote.NetworkErrorMapper
import com.wisekoin.ui.components.Transaction
import com.wisekoin.ui.components.sampleTransactions
import com.wisekoin.ui.theme.LemonDark
import com.wisekoin.ui.theme.MintDark
import com.wisekoin.ui.theme.PeachDark
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
import java.util.Date
import java.util.Locale

data class ReceiptUiState(
    val imageUri: Uri? = null,
    val storeName: String = "",
    val amount: String = "",
    val category: String = "Dining",
    val date: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class ReceiptViewModel : ViewModel() {

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
        _uiState.update {
            it.copy(
                imageUri = uri,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.receiptApi.extractReceipt(filePart)

                val formattedAmount = try {
                    NumberFormat.getNumberInstance(Locale.US).format(response.amount.toLong())
                } catch (e: Exception) {
                    response.amount.toLong().toString()
                }

                _uiState.update {
                    it.copy(
                        storeName = response.storeName,
                        category = response.category,
                        amount = formattedAmount,
                        isLoading = false
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        storeName = "Not available",
                        category = "Not available",
                        amount = "Not available",
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

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    private fun resetDateTime() {
        val now = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        _uiState.update { it.copy(date = "Today,\n$now") }
    }

    fun confirmAndSave(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val displayAmount = if (currentState.amount.isNotBlank()) "-${currentState.amount} ₫" else "-0 ₫"

        val categoryColor = when (currentState.category.lowercase()) {
            "dining" -> PeachDark
            "transport" -> LemonDark
            else -> MintDark
        }

        val newTransaction = Transaction(
            id = System.currentTimeMillis().toString(),
            title = currentState.storeName.ifBlank { "Uncategorized Store" },
            category = currentState.category.ifBlank { "General" },
            amount = displayAmount,
            isExpense = true,
            time = "Today, " + SimpleDateFormat("HH:mm", Locale.US).format(Date()),
            iconBgColor = categoryColor
        )

        sampleTransactions.add(0, newTransaction)

        resetState()
        onSuccess()
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
            .firstOrNull { it.name == currentState.category }
        if (selectedCategory == null) {
            onFailure("Category not found: ${currentState.category}")
            return
        }

        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                RetrofitClient.transactionApi.createTransaction(
                    TransactionRequest(
                        walletId = selectedWalletId,
                        categoryId = selectedCategory.id,
                        amount = parsedAmount,
                        type = "EXPENSE",
                        note = currentState.storeName.ifBlank { null },
                        transactionDate = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
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

    fun saveFromReceipt(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentState = _uiState.value
        val parsedAmount = currentState.amount.replace(",", "").toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (parsedAmount <= BigDecimal.ZERO) {
            onFailure("Invalid amount")
            return
        }
        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                RetrofitClient.transactionApi.createTransaction(
                    TransactionRequest(
                        walletId = 1L,
                        categoryId = 1L,
                        amount = parsedAmount,
                        type = "EXPENSE",
                        note = currentState.storeName.ifBlank { null }
                    )
                )
                resetState()
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                onFailure(NetworkErrorMapper.message(e, "Unable to save this transaction. Please try again."))
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
