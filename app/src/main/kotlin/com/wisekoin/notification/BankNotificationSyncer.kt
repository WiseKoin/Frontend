package com.wisekoin.notification

import com.wisekoin.data.local.BankNotificationCandidateStore
import com.wisekoin.data.model.BankNotificationSuggestionRequest
import com.wisekoin.data.model.NotificationMetricBatchRequest
import com.wisekoin.data.model.NotificationMetricDeltaDto
import com.wisekoin.data.repository.BankNotificationSuggestionRepository
import java.time.ZoneId
import java.util.Locale

class BankNotificationSyncer(
    private val store: BankNotificationCandidateStore,
    private val repository: BankNotificationSuggestionRepository,
    private val localeProvider: () -> String = { Locale.getDefault().toLanguageTag() },
    private val timeZoneProvider: () -> String = { ZoneId.systemDefault().id }
) {
    suspend fun sync() {
        if (!store.state.value.trackingEnabled) return
        store.state.value.candidates
            .filter { it.suggestionId == null }
            .forEach { candidate ->
                val response = repository.create(
                    BankNotificationSuggestionRequest(
                        source = candidate.sourceId,
                        sourceReferenceHash = candidate.id,
                        parserVersion = candidate.parserVersion,
                        amount = candidate.amount,
                        currency = candidate.currency,
                        type = candidate.direction.name,
                        occurredAt = candidate.occurredAt,
                        locale = localeProvider(),
                        timeZone = timeZoneProvider()
                    )
                )
                if (response.status == "PENDING") {
                    store.attachSuggestion(candidate.id, response.id)
                } else {
                    store.remove(candidate.id)
                }
            }

        val metrics = store.metricSnapshot()
        metrics.chunked(20).forEach { batch ->
            repository.recordMetrics(
                NotificationMetricBatchRequest(
                    batch.map {
                        NotificationMetricDeltaDto(
                            source = it.sourceId,
                            parserVersion = it.parserVersion,
                            metricDate = it.metricDate,
                            observed = it.observed,
                            parsed = it.parsed,
                            parseFailures = it.parseFailures,
                            localDuplicates = it.localDuplicates
                        )
                    }
                )
            )
            store.acknowledgeMetrics(batch)
        }
    }
}
