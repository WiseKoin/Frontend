package com.wisekoin.notification

import com.wisekoin.data.local.BankNotificationCandidateStore
import com.wisekoin.data.local.BankNotificationStoreState
import com.wisekoin.data.model.AiSuggestionResponseDto
import com.wisekoin.data.model.BankNotificationCandidate
import com.wisekoin.data.model.BankNotificationDirection
import com.wisekoin.data.model.BankNotificationMetric
import com.wisekoin.data.model.BankNotificationSuggestionRequest
import com.wisekoin.data.model.NotificationMetricBatchRequest
import com.wisekoin.data.repository.BankNotificationSuggestionRepository
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BankNotificationSyncerTest {
    @Test
    fun `sync uploads only deidentified candidate fields and acknowledges metrics`() = runBlocking {
        val candidate = BankNotificationCandidate(
            id = "a".repeat(64),
            sourceId = "VCB",
            parserVersion = "vcb-v1",
            amount = BigDecimal("125000"),
            currency = "VND",
            direction = BankNotificationDirection.EXPENSE,
            sourceApp = "VCB Digibank",
            occurredAt = "2026-10-25T09:00:00",
            capturedAtEpochMillis = 1_800_000_000_000
        )
        val metric = BankNotificationMetric(
            sourceId = "VCB",
            parserVersion = "vcb-v1",
            metricDate = "2026-10-25",
            observed = 2,
            parsed = 1,
            parseFailures = 1
        )
        val store = FakeStore(candidate, metric)
        val repository = FakeRepository()

        BankNotificationSyncer(
            store = store,
            repository = repository,
            localeProvider = { "vi-VN" },
            timeZoneProvider = { "Asia/Ho_Chi_Minh" }
        ).sync()

        assertEquals("VCB", repository.created.single().source)
        assertEquals(candidate.id, repository.created.single().sourceReferenceHash)
        assertEquals("suggestion-1", store.state.value.candidates.single().suggestionId)
        assertEquals(2, repository.metrics.single().metrics.single().observed)
        assertTrue(store.metricSnapshot().isEmpty())
    }

    @Test
    fun `sync removes a replay whose server suggestion is already terminal`() = runBlocking {
        val candidate = candidate()
        val store = FakeStore(candidate, metric = null)
        val repository = FakeRepository(status = "CONFIRMED")

        BankNotificationSyncer(store, repository).sync()

        assertTrue(store.state.value.candidates.isEmpty())
    }

    private fun candidate() = BankNotificationCandidate(
        id = "a".repeat(64),
        sourceId = "VCB",
        parserVersion = "vcb-v1",
        amount = BigDecimal("125000"),
        currency = "VND",
        direction = BankNotificationDirection.EXPENSE,
        sourceApp = "VCB Digibank",
        occurredAt = "2026-10-25T09:00:00",
        capturedAtEpochMillis = 1_800_000_000_000
    )

    private class FakeRepository(private val status: String = "PENDING") :
        BankNotificationSuggestionRepository {
        val created = mutableListOf<BankNotificationSuggestionRequest>()
        val metrics = mutableListOf<NotificationMetricBatchRequest>()

        override suspend fun create(
            request: BankNotificationSuggestionRequest
        ): AiSuggestionResponseDto {
            created += request
            return AiSuggestionResponseDto("suggestion-1", status)
        }

        override suspend fun recordMetrics(request: NotificationMetricBatchRequest) {
            metrics += request
        }

        override suspend fun reject(suggestionId: String, reason: String) = Unit
    }

    private class FakeStore(
        candidate: BankNotificationCandidate,
        metric: BankNotificationMetric?
    ) : BankNotificationCandidateStore {
        private val mutableState = MutableStateFlow(
            BankNotificationStoreState(
                trackingEnabled = true,
                activeAccountHash = "account",
                candidates = listOf(candidate),
                metrics = listOfNotNull(metric)
            )
        )
        override val state: StateFlow<BankNotificationStoreState> = mutableState

        override fun bindAccount(accountIdentifier: String?) = Unit
        override fun setTrackingEnabled(enabled: Boolean) = Unit
        override fun add(candidate: BankNotificationCandidate) = Unit
        override fun recordObservation(
            sourceId: String,
            parserVersion: String,
            candidate: BankNotificationCandidate?
        ): Boolean = false

        override fun attachSuggestion(candidateId: String, suggestionId: String) {
            mutableState.value = mutableState.value.copy(
                candidates = mutableState.value.candidates.map {
                    if (it.id == candidateId) it.copy(suggestionId = suggestionId) else it
                }
            )
        }

        override fun metricSnapshot(): List<BankNotificationMetric> = mutableState.value.metrics

        override fun acknowledgeMetrics(snapshot: List<BankNotificationMetric>) {
            mutableState.value = mutableState.value.copy(metrics = emptyList())
        }

        override fun remove(candidateId: String) {
            mutableState.value = mutableState.value.copy(
                candidates = mutableState.value.candidates.filterNot { it.id == candidateId }
            )
        }
        override fun clearCandidates() = Unit
    }
}
