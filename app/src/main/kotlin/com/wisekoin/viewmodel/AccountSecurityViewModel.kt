package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.OtpPurpose
import com.wisekoin.data.remote.NetworkErrorMapper
import com.wisekoin.data.repository.AccountSecurityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountSecurityUiState(
    val purpose: OtpPurpose? = null,
    val destination: String? = null,
    val codeRequested: Boolean = false,
    val verified: Boolean = false,
    val resendSecondsRemaining: Long = 0,
    val isRequestingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class AccountSecurityViewModel(
    private val repository: AccountSecurityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountSecurityUiState())
    val uiState: StateFlow<AccountSecurityUiState> = _uiState.asStateFlow()
    private val submissionGate = SubmissionGate()
    private var verificationToken: String? = null
    private var countdownJob: Job? = null

    fun begin(purpose: OtpPurpose) {
        if (_uiState.value.purpose == purpose) return
        countdownJob?.cancel()
        verificationToken = null
        _uiState.value = AccountSecurityUiState(purpose = purpose)
    }

    fun requestCode() {
        val purpose = _uiState.value.purpose ?: return
        if (_uiState.value.isRequestingCode || _uiState.value.resendSecondsRemaining > 0) return
        _uiState.update { it.copy(isRequestingCode = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = repository.requestOtp(purpose)
                verificationToken = null
                _uiState.update {
                    it.copy(
                        destination = response.destination,
                        codeRequested = true,
                        verified = false,
                        isRequestingCode = false,
                        resendSecondsRemaining = response.resendAfterSeconds
                    )
                }
                startCountdown()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val mapped = NetworkErrorMapper.map(
                    error,
                    "Unable to send a verification code. Please try again."
                )
                _uiState.update {
                    it.copy(
                        isRequestingCode = false,
                        errorMessage = mapped.message,
                        resendSecondsRemaining = mapped.retryAfterSeconds
                            ?: it.resendSecondsRemaining
                    )
                }
                if (mapped.retryAfterSeconds != null) startCountdown()
            }
        }
    }

    fun verifyCode(code: String) {
        val purpose = _uiState.value.purpose ?: return
        if (_uiState.value.isVerifyingCode || !code.matches(Regex("\\d{6}"))) return
        _uiState.update { it.copy(isVerifyingCode = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                verificationToken = repository.verifyOtp(purpose, code)
                _uiState.update { it.copy(isVerifyingCode = false, verified = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isVerifyingCode = false,
                        verified = false,
                        errorMessage = NetworkErrorMapper.message(
                            error,
                            "Unable to verify this code. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit
    ) {
        val token = verificationToken ?: return
        submit(
            action = { repository.changePassword(currentPassword, newPassword, token) },
            fallback = "Unable to change your password. Please try again.",
            onSuccess = onSuccess
        )
    }

    fun deleteAccount(currentPassword: String, onSuccess: () -> Unit) {
        val token = verificationToken ?: return
        submit(
            action = { repository.deleteAccount(currentPassword, token) },
            fallback = "Unable to delete your account. Please try again.",
            onSuccess = onSuccess
        )
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun submit(
        action: suspend () -> Unit,
        fallback: String,
        onSuccess: () -> Unit
    ) {
        if (!submissionGate.tryStart()) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                action()
                verificationToken = null
                _uiState.update { it.copy(isSubmitting = false, verified = false) }
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = NetworkErrorMapper.message(error, fallback)
                    )
                }
            } finally {
                submissionGate.finish()
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_uiState.value.resendSecondsRemaining > 0) {
                delay(1_000)
                _uiState.update {
                    it.copy(resendSecondsRemaining = (it.resendSecondsRemaining - 1).coerceAtLeast(0))
                }
            }
        }
    }
}
