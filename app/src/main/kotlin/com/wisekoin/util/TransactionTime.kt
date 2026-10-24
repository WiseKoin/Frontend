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
    fun nowLocalIso(clock: Clock = Clock.systemDefaultZone()): String =
        LocalDateTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
