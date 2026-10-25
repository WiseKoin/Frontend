package com.wisekoin.data.local

import com.wisekoin.data.model.BankNotificationCandidate
import com.wisekoin.data.model.BankNotificationDirection
import java.math.BigDecimal
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class BankNotificationRetentionTest {
    private val now = 1_800_000_000_000L

    @Test
    fun `drops candidates older than fourteen days`() {
        val recent = candidate("recent", now - Duration.ofDays(13).toMillis())
        val expired = candidate("expired", now - Duration.ofDays(15).toMillis())

        assertEquals(
            listOf(recent),
            BankNotificationRetention.retainCurrent(listOf(recent, expired), now)
        )
    }

    @Test
    fun `deduplicates newest candidate and caps queue`() {
        val existing = (0..100).map { index -> candidate(index.toString(), now - index) }
        val replacement = candidate("50", now)

        val result = BankNotificationRetention.add(existing, replacement, now)

        assertEquals(100, result.size)
        assertEquals(replacement, result.first())
        assertEquals(1, result.count { it.id == "50" })
    }

    @Test
    fun `does not add an already expired candidate`() {
        val expired = candidate("expired", now - Duration.ofDays(15).toMillis())

        assertEquals(emptyList<BankNotificationCandidate>(), BankNotificationRetention.add(
            candidates = emptyList(),
            candidate = expired,
            nowEpochMillis = now
        ))
    }

    private fun candidate(id: String, capturedAt: Long) = BankNotificationCandidate(
        id = id,
        amount = BigDecimal.TEN,
        currency = "VND",
        direction = BankNotificationDirection.EXPENSE,
        sourceApp = "Bank",
        occurredAt = "2026-01-01T10:00:00",
        capturedAtEpochMillis = capturedAt
    )
}
