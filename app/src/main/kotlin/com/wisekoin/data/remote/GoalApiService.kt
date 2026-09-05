package com.wisekoin.data.remote

import com.wisekoin.data.model.GoalRequestDto
import com.wisekoin.data.model.GoalResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GoalApiService {
    @GET("api/goals")
    suspend fun getGoals(): List<GoalResponseDto>

    @POST("api/goals")
    suspend fun createGoal(@Body request: GoalRequestDto): GoalResponseDto
}