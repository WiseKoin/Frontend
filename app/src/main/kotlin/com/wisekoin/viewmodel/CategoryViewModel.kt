package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.CategoryRequest
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<CategoryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CategoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    fun loadCategories(type: String, isLoggedIn: Boolean) {
        if (!isLoggedIn) {
            _uiState.update { it.copy(categories = emptyList(), isLoading = false, errorMessage = null) }
            return
        }

        val categoryTypeStr = when (type.lowercase()) {
            "income" -> "INCOME"
            "lend" -> "LEND"
            "borrow" -> "BORROW"
            else -> "EXPENSE"
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val list = RetrofitClient.categoryApi.getCategoriesByType(categoryTypeStr)
                _uiState.update { it.copy(categories = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        categories = emptyList(),
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Lỗi tải danh mục từ máy chủ"
                    )
                }
            }
        }
    }

    fun createCategory(
        name: String,
        type: String,
        icon: String?,
        isLoggedIn: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isLoggedIn) {
            onError("Please log in to create new category.")
            return
        }

        val categoryTypeStr = when (type.lowercase()) {
            "income" -> "INCOME"
            "lend" -> "LEND"
            "borrow" -> "BORROW"
            else -> "EXPENSE"
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                RetrofitClient.categoryApi.createCategory(
                    CategoryRequest(
                        name = name.trim(),
                        type = categoryTypeStr,
                        icon = icon
                    )
                )
                loadCategories(type, isLoggedIn)
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Create a new category failed."
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                onError(errorMsg)
            }
        }
    }
}
