package com.wisekoin.data.remote

import com.wisekoin.data.model.GoalFundRequestDto
import com.wisekoin.data.model.GoalRequestDto
import com.wisekoin.data.model.GoalResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GoalApiService {
    @GET("api/goals")
    suspend fun getGoals(): List<GoalResponseDto>

    @POST("api/goals")
    suspend fun createGoal(@Body request: GoalRequestDto): GoalResponseDto

    @POST("api/goals/{id}/fund")
    suspend fun fundGoal(
        @Path("id") goalId: Long,
        @Body request: GoalFundRequestDto
    ): GoalResponseDto
}
