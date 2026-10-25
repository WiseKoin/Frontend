package com.wisekoin.data.remote

import com.wisekoin.data.model.AiSuggestionRejectionRequest
import com.wisekoin.data.model.AiSuggestionResponseDto
import com.wisekoin.data.model.BankNotificationSuggestionRequest
import com.wisekoin.data.model.NotificationMetricBatchRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AiSuggestionApiService {
    @POST("api/ai/suggestions/notifications")
    suspend fun createNotificationSuggestion(
        @Body request: BankNotificationSuggestionRequest
    ): AiSuggestionResponseDto

    @POST("api/ai/suggestions/notifications/metrics")
    suspend fun recordNotificationMetrics(@Body request: NotificationMetricBatchRequest)

    @POST("api/ai/suggestions/{id}/rejection")
    suspend fun rejectSuggestion(
        @Path("id") suggestionId: String,
        @Body request: AiSuggestionRejectionRequest
    ): AiSuggestionResponseDto
}
