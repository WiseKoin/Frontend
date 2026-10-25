package com.wisekoin.data.model

import java.math.BigDecimal

enum class BankNotificationDirection {
    EXPENSE,
    INCOME
}

data class BankNotificationCandidate(
    val id: String,
    val sourceId: String = "",
    val parserVersion: String = "",
    val amount: BigDecimal,
    val currency: String,
    val direction: BankNotificationDirection,
    val sourceApp: String,
    val occurredAt: String,
    val capturedAtEpochMillis: Long,
    val suggestionId: String? = null
)

data class BankNotificationMetric(
    val sourceId: String,
    val parserVersion: String,
    val metricDate: String,
    val observed: Long = 0,
    val parsed: Long = 0,
    val parseFailures: Long = 0,
    val localDuplicates: Long = 0
)
