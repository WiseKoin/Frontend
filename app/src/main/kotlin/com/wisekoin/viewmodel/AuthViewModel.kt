package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.local.SessionTokenStore
import com.wisekoin.data.model.LoginRequest
import com.wisekoin.data.model.RefreshTokenRequest
import com.wisekoin.data.model.RegisterRequest
import com.wisekoin.data.model.UpdateProfileRequest
import com.wisekoin.data.model.UserResponseDto
import com.wisekoin.data.remote.ErrorResponse
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.data.remote.NetworkErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "Guest",
    val avatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuAvZLwLc8DTKzGGRrKq1WOp54IgsHb5C9gRSqjUlBv_xmhBsjyJro6RV5bh6tEEr27KHFYEB3HD5GJ_nVoJtWJHkjLNhCWqS3mi7TPCVUkwM0mQ2DT6icCYuGjo0hFRF0P5qP5bj6fHaGdx4ZyT57CHAWMX4ZciqJwkYAc88NUwzWebC7-lnAN-6ps1ZieMqDjK0vlIBgAGlZaTL_wN0P85oGyONEChyuDc153m7n9zyAARuKqyBorM9Q",
    val isLoggedIn: Boolean = false,
    val currentUser: UserResponseDto? = null,
    val sessionStatus: SessionStatus = SessionStatus.RESOLVING,
    val sessionResolutionFailed: Boolean = false,
    val sessionExpiryVersion: Int = 0,
    val isLoading: Boolean = false,
    val registerErrorMessage: String? = null,
    val loginErrorMessage: String? = null,
    val updateErrorMessage: String? = null
)

enum class SessionStatus {
    RESOLVING,
    AUTHENTICATED,
    GUEST,
    ERROR
}

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var tokenManager: SessionTokenStore? = null
    private val authSubmissionGate = SubmissionGate()

    fun setTokenManager(manager: SessionTokenStore) {
        this.tokenManager = manager
        if (manager.hasSession()) {
            resolveStoredSession()
        } else {
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    name = "Guest",
                    currentUser = null,
                    sessionStatus = SessionStatus.GUEST,
                    sessionResolutionFailed = false,
                    isLoading = false
                )
            }
        }
    }

    fun retrySessionResolution() {
        if (tokenManager?.hasSession() == true) {
            resolveStoredSession()
        } else {
            onSessionExpired()
        }
    }

    fun onSessionExpired() {
        tokenManager?.clearTokens()
        _uiState.update { current ->
            val alreadyExpired = current.sessionStatus == SessionStatus.GUEST &&
                current.sessionExpiryVersion > 0
            current.copy(
                isLoggedIn = false,
                currentUser = null,
                name = "Guest",
                password = "",
                isLoading = false,
                sessionStatus = SessionStatus.GUEST,
                sessionResolutionFailed = false,
                sessionExpiryVersion = if (alreadyExpired) {
                    current.sessionExpiryVersion
                } else {
                    current.sessionExpiryVersion + 1
                }
            )
        }
    }

    private fun resolveStoredSession() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                isLoading = true,
                sessionStatus = SessionStatus.RESOLVING,
                sessionResolutionFailed = false
            )
        }
        fetchUser(isSessionResolution = true)
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_uiState.value.isLoading) return
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank()) {
            val msg = "Vui lòng nhập địa chỉ Email"
            _uiState.update { it.copy(loginErrorMessage = msg) }
            onError(msg)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.contains("@")) {
            val msg = "Định dạng Email không hợp lệ"
            _uiState.update { it.copy(loginErrorMessage = msg) }
            onError(msg)
            return
        }

        if (password.isBlank()) {
            val msg = "Vui lòng nhập Mật khẩu"
            _uiState.update { it.copy(loginErrorMessage = msg) }
            onError(msg)
            return
        }
        if (!authSubmissionGate.tryStart()) return

        _uiState.update { it.copy(isLoading = true, loginErrorMessage = null) }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.login(LoginRequest(email, password))
                tokenManager?.saveTokens(response.accessToken, response.refreshToken)
                fetchUser(isSessionResolution = true, onSuccess = onSuccess)
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                val errorMsg = if ((e as? HttpException)?.code() == 401) {
                    "The email or password is incorrect."
                } else {
                    NetworkErrorMapper.message(e, "Unable to sign in. Please try again.")
                }
                _uiState.update { it.copy(isLoading = false, loginErrorMessage = errorMsg) }
                onError(errorMsg)
            } finally {
                authSubmissionGate.finish()
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
        if (_uiState.value.isLoading) return
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            onError("Please enter the required information.")
            return
        }
        if (!authSubmissionGate.tryStart()) return

        _uiState.update { it.copy(isLoading = true, registerErrorMessage = null) }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.authApi.register(
                    RegisterRequest(username = username, email = email, password = password, phone = phone)
                )
                tokenManager?.saveTokens(response.accessToken, response.refreshToken)
                _uiState.update {
                    it.copy(
                        isLoggedIn = false,
                        isLoading = false,
                        email = email,
                        sessionStatus = SessionStatus.RESOLVING
                    )
                }
                fetchUser(isSessionResolution = true, onSuccess = onSuccess)
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                val errorMsg = NetworkErrorMapper.message(e, "Unable to create your account. Please try again.")
                _uiState.update { it.copy(isLoading = false, registerErrorMessage = errorMsg) }
                onError(errorMsg)
            } finally {
                authSubmissionGate.finish()
            }
        }
    }

    fun loginWithSocial(provider: String, onSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                sessionStatus = SessionStatus.AUTHENTICATED,
                name = "User ($provider)",
                email = "user@$provider.com"
            )
        }
        onSuccess()
    }

    fun logout(onComplete: () -> Unit = {}) {
        val manager = tokenManager
        val refreshToken = manager?.getRefreshToken()
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                if (!refreshToken.isNullOrBlank()) {
                    RetrofitClient.authApi.logout(RefreshTokenRequest(refreshToken))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Local logout must still complete if the API is unavailable.
            } finally {
                manager?.clearTokens()
                _uiState.update {
                    it.copy(
                        isLoggedIn = false,
                        currentUser = null,
                        name = "Guest",
                        email = "",
                        password = "",
                        isLoading = false,
                        sessionStatus = SessionStatus.GUEST,
                        sessionResolutionFailed = false
                    )
                }
                onComplete()
            }
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

        _uiState.update { it.copy(isLoading = true, updateErrorMessage = null) }

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
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                val errorMsg = NetworkErrorMapper.message(e, "Unable to update your profile. Please try again.")
                _uiState.update { it.copy(isLoading = false, updateErrorMessage = errorMsg) }
                onError(errorMsg)
            }
        }
    }

    private fun fetchUser(
        isSessionResolution: Boolean = false,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        name = if (user.username.isNotBlank()) user.username else user.email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = user.email,
                        isLoading = false,
                        isLoggedIn = true,
                        sessionStatus = SessionStatus.AUTHENTICATED,
                        sessionResolutionFailed = false
                    )
                }
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (tokenManager?.hasSession() != true) {
                    onSessionExpired()
                } else if (isSessionResolution) {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = false,
                            isLoading = false,
                            sessionStatus = SessionStatus.ERROR,
                            sessionResolutionFailed = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            updateErrorMessage = "Unable to refresh your profile.",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
