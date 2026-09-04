package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.UserResponseDto
import com.wisekoin.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserUiState(
    val currentUser: UserResponseDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun fetchCurrentUser() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val user = RetrofitClient.userApi.getCurrentUser()
                _uiState.update { it.copy(currentUser = user, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        currentUser = null,
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Không thể tải thông tin người dùng"
                    )
                }
            }
        }
    }

    fun refreshUser() {
        fetchCurrentUser()
    }
}
