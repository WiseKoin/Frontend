package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val email: String = "phat.hoang@wisekoin.app",
    val password: String = "••••••••",
    val name: String = "Phát Hoàng",
    val avatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuAvZLwLc8DTKzGGRrKq1WOp54IgsHb5C9gRSqjUlBv_xmhBsjyJro6RV5bh6tEEr27KHFYEB3HD5GJ_nVoJtWJHkjLNhCWqS3mi7TPCVUkwM0mQ2DT6icCYuGjo0hFRF0P5qP5bj6fHaGdx4ZyT57CHAWMX4ZciqJwkYAc88NUwzWebC7-lnAN-6ps1ZieMqDjK0vlIBgAGlZaTL_wN0P85oGyONEChyuDc153m7n9zyAARuKqyBorM9Q",
    val isPro: Boolean = true,
    val level: Int = 12,
    val isLoggedIn: Boolean = true,
    val isLoading: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_uiState.value.email.isBlank()) {
            onError("Vui lòng nhập địa chỉ Email")
            return
        }
        _uiState.update { it.copy(isLoggedIn = true) }
        onSuccess()
    }

    fun loginWithSocial(provider: String, onSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                name = "Phát Hoàng ($provider)",
                email = "phat.hoang@$provider.com"
            )
        }
        onSuccess()
    }

    fun logout() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    fun updateAvatar(newUrl: String) {
        _uiState.update { it.copy(avatarUrl = newUrl) }
    }
}
