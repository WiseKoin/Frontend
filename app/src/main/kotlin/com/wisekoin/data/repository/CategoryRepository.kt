package com.wisekoin.data.repository

import com.wisekoin.data.model.CategoryRequest
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.data.model.CategoryUpdateRequest
import com.wisekoin.data.remote.CategoryApiService

interface CategoryRepository {
    suspend fun getCategories(type: String): List<CategoryResponse>
    suspend fun createCategory(request: CategoryRequest): CategoryResponse
    suspend fun updateCategory(categoryId: Long, request: CategoryUpdateRequest): CategoryResponse
    suspend fun deleteCategory(categoryId: Long)
}

class NetworkCategoryRepository(
    private val api: CategoryApiService
) : CategoryRepository {
    override suspend fun getCategories(type: String): List<CategoryResponse> =
        api.getCategoriesByType(type)

    override suspend fun createCategory(request: CategoryRequest): CategoryResponse =
        api.createCategory(request)

    override suspend fun updateCategory(
        categoryId: Long,
        request: CategoryUpdateRequest
    ): CategoryResponse = api.updateCategory(categoryId, request)

    override suspend fun deleteCategory(categoryId: Long) = api.deleteCategory(categoryId)
}
