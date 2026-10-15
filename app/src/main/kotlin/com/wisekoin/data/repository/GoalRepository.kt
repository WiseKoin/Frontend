package com.wisekoin.data.repository

import com.wisekoin.data.model.GoalFundRequestDto
import com.wisekoin.data.model.GoalRequestDto
import com.wisekoin.data.model.GoalResponseDto
import com.wisekoin.data.remote.GoalApiService

interface GoalRepository {
    suspend fun getGoals(): List<GoalResponseDto>
    suspend fun createGoal(request: GoalRequestDto): GoalResponseDto
    suspend fun fundGoal(goalId: Long, request: GoalFundRequestDto): GoalResponseDto
    suspend fun deleteGoal(goalId: Long)
}

class NetworkGoalRepository(
    private val api: GoalApiService
) : GoalRepository {
    override suspend fun getGoals(): List<GoalResponseDto> = api.getGoals()

    override suspend fun createGoal(request: GoalRequestDto): GoalResponseDto = api.createGoal(request)

    override suspend fun fundGoal(
        goalId: Long,
        request: GoalFundRequestDto
    ): GoalResponseDto = api.fundGoal(goalId, request)

    override suspend fun deleteGoal(goalId: Long) = api.deleteGoal(goalId)
}
