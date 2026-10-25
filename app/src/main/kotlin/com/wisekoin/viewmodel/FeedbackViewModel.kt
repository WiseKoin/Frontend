package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.FeedbackCategory
import com.wisekoin.data.model.FeedbackSubmission
import com.wisekoin.data.repository.FeedbackRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FeedbackValidationError {
    MESSAGE_REQUIRED,
    MESSAGE_TOO_SHORT,
    INVALID_EMAIL
}

enum class FeedbackSubmissionState {
    IDLE,
    SUBMITTING,
    SENT,
    ERROR
}

data class FeedbackUiState(
    val category: FeedbackCategory = FeedbackCategory.BUG,
    val message: String = "",
    val name: String = "",
    val email: String = "",
    val validationError: FeedbackValidationError? = null,
    val submissionState: FeedbackSubmissionState = FeedbackSubmissionState.IDLE
)

class FeedbackViewModel(
    private val repository: FeedbackRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val mutableState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = mutableState.asStateFlow()

    fun prefillContact(name: String?, email: String?) {
        mutableState.update { current ->
            current.copy(
                name = current.name.ifBlank { name.orEmpty().take(MAX_NAME_LENGTH) },
                email = current.email.ifBlank { email.orEmpty().take(MAX_EMAIL_LENGTH) }
            )
        }
    }

    fun selectCategory(category: FeedbackCategory) {
        if (mutableState.value.submissionState == FeedbackSubmissionState.SUBMITTING) return
        mutableState.update {
            it.copy(
                category = category,
                validationError = null,
                submissionState = FeedbackSubmissionState.IDLE
            )
        }
    }

    fun updateMessage(message: String) {
        if (mutableState.value.submissionState == FeedbackSubmissionState.SUBMITTING) return
        mutableState.update {
            it.copy(
                message = message.take(MAX_MESSAGE_LENGTH),
                validationError = null,
                submissionState = FeedbackSubmissionState.IDLE
            )
        }
    }

    fun updateName(name: String) {
        if (mutableState.value.submissionState == FeedbackSubmissionState.SUBMITTING) return
        mutableState.update {
            it.copy(
                name = name.take(MAX_NAME_LENGTH),
                submissionState = FeedbackSubmissionState.IDLE
            )
        }
    }

    fun updateEmail(email: String) {
        if (mutableState.value.submissionState == FeedbackSubmissionState.SUBMITTING) return
        mutableState.update {
            it.copy(
                email = email.take(MAX_EMAIL_LENGTH),
                validationError = null,
                submissionState = FeedbackSubmissionState.IDLE
            )
        }
    }

    fun submit() {
        val current = mutableState.value
        if (current.submissionState == FeedbackSubmissionState.SUBMITTING) return

        val validationError = validate(current)
        if (validationError != null) {
            mutableState.update { it.copy(validationError = validationError) }
            return
        }

        mutableState.update {
            it.copy(
                validationError = null,
                submissionState = FeedbackSubmissionState.SUBMITTING
            )
        }

        viewModelScope.launch(dispatcher) {
            val result = try {
                repository.submit(
                    FeedbackSubmission(
                        category = current.category,
                        message = current.message.trim(),
                        name = current.name.trim().takeIf(String::isNotEmpty),
                        email = current.email.trim().takeIf(String::isNotEmpty)
                    )
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                Result.failure(IllegalStateException("Feedback submission failed."))
            }

            mutableState.update { latest ->
                if (result.isSuccess) {
                    latest.copy(
                        message = "",
                        validationError = null,
                        submissionState = FeedbackSubmissionState.SENT
                    )
                } else {
                    latest.copy(submissionState = FeedbackSubmissionState.ERROR)
                }
            }
        }
    }

    fun sendAnother() {
        mutableState.update {
            it.copy(
                message = "",
                validationError = null,
                submissionState = FeedbackSubmissionState.IDLE
            )
        }
    }

    private fun validate(state: FeedbackUiState): FeedbackValidationError? = when {
        state.message.isBlank() -> FeedbackValidationError.MESSAGE_REQUIRED
        state.message.trim().length < MIN_MESSAGE_LENGTH ->
            FeedbackValidationError.MESSAGE_TOO_SHORT
        state.email.isNotBlank() && !EMAIL_PATTERN.matches(state.email.trim()) ->
            FeedbackValidationError.INVALID_EMAIL
        else -> null
    }

    companion object {
        const val MIN_MESSAGE_LENGTH = 12
        const val MAX_MESSAGE_LENGTH = 2_000
        const val MAX_NAME_LENGTH = 100
        const val MAX_EMAIL_LENGTH = 254

        private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
