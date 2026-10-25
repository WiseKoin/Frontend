package com.wisekoin.data.local

import com.wisekoin.data.model.BankNotificationCandidate
import java.time.Duration

object BankNotificationRetention {
    private const val MAX_CANDIDATES = 100
    private val retention = Duration.ofDays(14)

    fun retainCurrent(
        candidates: List<BankNotificationCandidate>,
        nowEpochMillis: Long
    ): List<BankNotificationCandidate> {
        val cutoff = nowEpochMillis - retention.toMillis()
        return candidates
            .filter { it.capturedAtEpochMillis >= cutoff }
            .take(MAX_CANDIDATES)
    }

    fun add(
        candidates: List<BankNotificationCandidate>,
        candidate: BankNotificationCandidate,
        nowEpochMillis: Long
    ): List<BankNotificationCandidate> {
        val retained = retainCurrent(candidates, nowEpochMillis)
        val cutoff = nowEpochMillis - retention.toMillis()
        if (candidate.capturedAtEpochMillis < cutoff) return retained
        return (listOf(candidate) + retained.filterNot { it.id == candidate.id })
            .take(MAX_CANDIDATES)
    }
}
