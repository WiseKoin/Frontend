package com.wisekoin.data.model

import java.math.BigDecimal

enum class BankNotificationDirection {
    EXPENSE,
    INCOME
}

data class BankNotificationCandidate(
    val id: String,
    val amount: BigDecimal,
    val currency: String,
    val direction: BankNotificationDirection,
    val sourceApp: String,
    val occurredAt: String,
    val capturedAtEpochMillis: Long
)
