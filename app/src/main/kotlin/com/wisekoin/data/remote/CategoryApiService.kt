package com.wisekoin.data.remote

import com.wisekoin.data.model.CategoryRequest
import com.wisekoin.data.model.CategoryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CategoryApiService {
    @GET("api/categories")
    suspend fun getCategoriesByType(
        @Query("categoryType") categoryType: String
    ): List<CategoryResponse>

    @POST("api/categories")
    suspend fun createCategory(
        @Body request: CategoryRequest
    ): CategoryResponse
}
