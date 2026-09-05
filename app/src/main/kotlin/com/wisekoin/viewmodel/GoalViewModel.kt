package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.GoalRequestDto
import com.wisekoin.data.model.GoalResponseDto
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.remote.ErrorResponse
import com.wisekoin.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class GoalUiState(
    val goals: List<GoalResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isFunding: Boolean = false,
    val errorMessage: String? = null
)

class GoalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    val totalSaved: BigDecimal
        get() = _uiState.value.goals.sumOf { it.currentAmount }

    val activeGoals: List<GoalResponseDto>
        get() = _uiState.value.goals.filter { it.status == "IN_PROGRESS" }

    fun loadGoals() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.goalApi.getGoals()
                _uiState.update { it.copy(goals = response, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to load goals"
                    )
                }
            }
        }
    }

    fun createGoal(
        name: String,
        targetAmount: BigDecimal,
        deadline: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.update { it.copy(isCreating = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                RetrofitClient.goalApi.createGoal(
                    GoalRequestDto(
                        name = name.trim(),
                        targetAmount = targetAmount,
                        deadline = deadline
                    )
                )
                loadGoals()
                _uiState.update { it.copy(isCreating = false) }
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = extractBackendMessage(e)
                _uiState.update { it.copy(isCreating = false, errorMessage = errorMsg) }
                onError(errorMsg)
            }
        }
    }

    fun fundGoal(
        goalId: Long,
        walletId: Long,
        categoryId: Long,
        amount: BigDecimal,
        note: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.update { it.copy(isFunding = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                RetrofitClient.transactionApi.createTransaction(
                    TransactionRequest(
                        walletId = walletId,
                        categoryId = categoryId,
                        amount = amount,
                        type = "TRANSFER",
                        note = note,
                        goalId = goalId
                    )
                )
                loadGoals()
                _uiState.update { it.copy(isFunding = false) }
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = extractBackendMessage(e)
                _uiState.update { it.copy(isFunding = false, errorMessage = errorMsg) }
                onError(errorMsg)
            }
        }
    }

    private fun extractBackendMessage(e: Exception): String {
        val message = e.message ?: "Failed to process request"
        return when {
            message.contains("Insufficient", ignoreCase = true) -> "Insufficient wallet balance."
            message.contains("must not be blank") || message.contains("must not be null") ->
                "Validation failed. Please check your input."
            message.contains("Goal not found") || message.contains("404") -> "Goal not found."
            message.contains("Wallet not found") || message.contains("Category not found") ->
                "Invalid wallet or category selection."
            message.startsWith("HTTP 400:") -> {
                val body = message.substringAfter("HTTP 400: ")
                when {
                    body.contains("error") -> {
                        try {
                            val parsed = RetrofitClient.getGson().fromJson(body, ErrorResponse::class.java)
                            parsed?.error ?: parsed?.message ?: "Bad request. Please check your input."
                        } catch (ex: Exception) {
                            "Bad request. Please check your input."
                        }
                    }
                    else -> "Bad request. Please check your input."
                }
            }
            else -> message
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}