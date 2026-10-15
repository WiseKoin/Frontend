package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.remote.NetworkErrorMapper
import com.wisekoin.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PasswordRecoveryUiState(
    val email: String = "",
    val verificationToken: String? = null,
    val isSubmitting: Boolean = false,
    val resendSeconds: Long = 0,
    val errorMessage: String? = null
)

class PasswordRecoveryViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState.asStateFlow()
    private val submissionGate = SubmissionGate()
    private var countdownJob: Job? = null

    fun requestCode(email: String, onSuccess: () -> Unit) {
        val normalizedEmail = email.trim()
        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(email = normalizedEmail, isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = repository.requestPasswordRecovery(normalizedEmail)
                _uiState.update { it.copy(isSubmitting = false) }
                startCountdown(response.resendAfterSeconds)
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = NetworkErrorMapper.message(
                            error,
                            "We couldn't send a verification code. Please try again."
                        )
                    )
                }
            } finally {
                submissionGate.finish()
            }
        }
    }

    fun resendCode() {
        if (_uiState.value.resendSeconds > 0 || _uiState.value.email.isBlank()) return
        requestCode(_uiState.value.email) {}
    }

    fun verifyCode(code: String, onSuccess: () -> Unit) {
        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val token = repository.verifyPasswordRecovery(_uiState.value.email, code)
                _uiState.update { it.copy(verificationToken = token, isSubmitting = false) }
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = NetworkErrorMapper.message(
                            error,
                            "The verification code is invalid or expired."
                        )
                    )
                }
            } finally {
                submissionGate.finish()
            }
        }
    }

    fun resetPassword(newPassword: String, onSuccess: () -> Unit) {
        val token = _uiState.value.verificationToken ?: return
        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.resetPassword(_uiState.value.email, newPassword, token)
                _uiState.update { PasswordRecoveryUiState(email = it.email) }
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = NetworkErrorMapper.message(
                            error,
                            "We couldn't reset your password. Request a new code and try again."
                        )
                    )
                }
            } finally {
                submissionGate.finish()
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun startCountdown(seconds: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (remaining in seconds downTo 1) {
                _uiState.update { it.copy(resendSeconds = remaining) }
                delay(1_000)
            }
            _uiState.update { it.copy(resendSeconds = 0) }
        }
    }
}
