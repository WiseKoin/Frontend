package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.local.TokenManager
import com.wisekoin.data.model.LoginRequest
import com.wisekoin.data.model.RegisterRequest
import com.wisekoin.data.model.UpdateProfileRequest
import com.wisekoin.data.model.UserResponseDto
import com.wisekoin.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "Guest",
    val avatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuAvZLwLc8DTKzGGRrKq1WOp54IgsHb5C9gRSqjUlBv_xmhBsjyJro6RV5bh6tEEr27KHFYEB3HD5GJ_nVoJtWJHkjLNhCWqS3mi7TPCVUkwM0mQ2DT6icCYuGjo0hFRF0P5qP5bj6fHaGdx4ZyT57CHAWMX4ZciqJwkYAc88NUwzWebC7-lnAN-6ps1ZieMqDjK0vlIBgAGlZaTL_wN0P85oGyONEChyuDc153m7n9zyAARuKqyBorM9Q",
    val isPro: Boolean = false,
    val level: Int = 1,
    val isLoggedIn: Boolean = false,
    val currentUser: UserResponseDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var tokenManager: TokenManager? = null

    fun setTokenManager(manager: TokenManager) {
        this.tokenManager = manager
        if (manager.hasToken()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoggedIn = true) }
                fetchUser()
            }
        } else {
            _uiState.update { it.copy(isLoggedIn = false, name = "Guest", currentUser = null) }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank()) {
            val msg = "Vui lòng nhập địa chỉ Email"
            _uiState.update { it.copy(errorMessage = msg) }
            onError(msg)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.contains("@")) {
            val msg = "Định dạng Email không hợp lệ"
            _uiState.update { it.copy(errorMessage = msg) }
            onError(msg)
            return
        }

        if (password.isBlank()) {
            val msg = "Vui lòng nhập Mật khẩu"
            _uiState.update { it.copy(errorMessage = msg) }
            onError(msg)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.login(LoginRequest(email, password))
                tokenManager?.saveToken(response.accessToken)
                fetchUser()
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin."
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                onError(errorMsg)
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        phone: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            onError("Vui lòng nhập đầy đủ thông tin bắt buộc")
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.register(
                    RegisterRequest(username = username, email = email, password = password, phone = phone)
                )
                tokenManager?.saveToken(response.accessToken)
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        isLoading = false,
                        email = email
                    )
                }
                fetchUser()
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
                onError(e.localizedMessage ?: "Đăng ký thất bại. Vui lòng kiểm tra lại.")
            }
        }
    }

    fun loginWithSocial(provider: String, onSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                name = "User ($provider)",
                email = "user@$provider.com"
            )
        }
        onSuccess()
    }

    fun logout() {
        tokenManager?.clearToken()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                name = "Guest",
                email = "",
                password = "",
                isLoading = false
            )
        }
    }

    fun updateAvatar(newUrl: String) {
        _uiState.update { it.copy(avatarUrl = newUrl) }
    }

    fun updateProfile(
        username: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentEmail = _uiState.value.email
        if (currentEmail.isBlank()) {
            onError("No email found. Please log in again.")
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.userApi.updateProfile(
                    UpdateProfileRequest(
                        username = username,
                        email = currentEmail,
                        phone = phone
                    )
                )
                _uiState.update {
                    it.copy(
                        currentUser = response,
                        name = response.username,
                        isLoading = false
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to update profile"
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                onError(errorMsg)
            }
        }
    }

    private fun fetchUser() {
        viewModelScope.launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        name = if (user.username.isNotBlank()) user.username else user.email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = user.email,
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
            } catch (e: Exception) {
                val fallback = _uiState.value.email.substringBefore("@").replaceFirstChar { it.uppercase() }
                _uiState.update { it.copy(name = fallback, isLoading = false) }
            }
        }
    }
}
