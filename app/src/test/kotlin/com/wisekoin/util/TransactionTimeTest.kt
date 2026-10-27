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

    @Test
    fun `display date uses day month year format`() {
        assertEquals(
            "05/09/2026",
            TransactionTime.formatDisplayDate("2026-09-05T14:07:32")
        )
    }

    @Test
    fun `display date accepts fractional seconds from the API`() {
        assertEquals(
            "27/10/2026",
            TransactionTime.formatDisplayDate("2026-10-27T16:45:12.123456")
        )
    }

    @Test
    fun `invalid display date remains visible for diagnosis`() {
        assertEquals("invalid", TransactionTime.formatDisplayDate("invalid"))
    }
}
