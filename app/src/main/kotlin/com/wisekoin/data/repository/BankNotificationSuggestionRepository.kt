package com.wisekoin.data.repository

import com.wisekoin.data.model.AiSuggestionRejectionRequest
import com.wisekoin.data.model.AiSuggestionResponseDto
import com.wisekoin.data.model.BankNotificationSuggestionRequest
import com.wisekoin.data.model.NotificationMetricBatchRequest
import com.wisekoin.data.remote.AiSuggestionApiService

interface BankNotificationSuggestionRepository {
    suspend fun create(request: BankNotificationSuggestionRequest): AiSuggestionResponseDto
    suspend fun recordMetrics(request: NotificationMetricBatchRequest)
    suspend fun reject(suggestionId: String, reason: String)
}

class NetworkBankNotificationSuggestionRepository(
    private val api: AiSuggestionApiService
) : BankNotificationSuggestionRepository {
    override suspend fun create(
        request: BankNotificationSuggestionRequest
    ): AiSuggestionResponseDto = api.createNotificationSuggestion(request)

    override suspend fun recordMetrics(request: NotificationMetricBatchRequest) {
        api.recordNotificationMetrics(request)
    }

    override suspend fun reject(suggestionId: String, reason: String) {
        api.rejectSuggestion(suggestionId, AiSuggestionRejectionRequest(reason))
    }
}
