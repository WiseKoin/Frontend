package com.wisekoin.util

import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * The backend transaction contract stores a user-local wall time without an offset.
 * The extraction API receives the device IANA zone separately, so fallback values use
 * the same device zone and are serialized as ISO_LOCAL_DATE_TIME.
 */
object TransactionTime {
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun nowLocalIso(clock: Clock = Clock.systemDefaultZone()): String =
        LocalDateTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    fun formatDisplayDate(value: String): String =
        runCatching {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .format(displayDateFormatter)
        }.getOrDefault(value)
}
