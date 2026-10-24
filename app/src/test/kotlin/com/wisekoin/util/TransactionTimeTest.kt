package com.wisekoin.util

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionTimeTest {
    @Test
    fun `fallback timestamp is local wall time in the selected zone`() {
        val clock = Clock.fixed(Instant.parse("2026-10-24T01:30:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"))

        assertEquals("2026-10-24T08:30:00", TransactionTime.nowLocalIso(clock))
    }
}
