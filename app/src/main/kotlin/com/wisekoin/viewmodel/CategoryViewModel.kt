package com.wisekoin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisekoin.data.model.CategoryRequest
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.data.remote.NetworkErrorMapper
import com.wisekoin.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

data class CategoryUiState(
    val categories: List<CategoryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var loadingType: String? = null

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

        if (loadJob?.isActive == true && loadingType == categoryTypeStr) return
        loadJob?.cancel()
        loadingType = categoryTypeStr

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            try {
                val list = repository.getCategories(categoryTypeStr)
                _uiState.update { it.copy(categories = list, isLoading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        categories = emptyList(),
                        isLoading = false,
                        errorMessage = NetworkErrorMapper.message(e, "Unable to load categories. Please try again.")
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
                repository.createCategory(
                    CategoryRequest(
                        name = name.trim(),
                        type = categoryTypeStr,
                        icon = icon
                    )
                )
                loadCategories(type, isLoggedIn)
                onSuccess()
            } catch (error: CancellationException) {
                throw error
            } catch (e: Exception) {
                val errorMsg = NetworkErrorMapper.message(e, "Unable to create the category. Please try again.")
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                onError(errorMsg)
            }
        }
    }
}
