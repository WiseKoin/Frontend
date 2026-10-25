package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BankNotificationSuggestionRequest(
    val source: String,
    @SerializedName("source_reference_hash")
    val sourceReferenceHash: String,
    @SerializedName("parser_version")
    val parserVersion: String,
    val amount: BigDecimal,
    val currency: String,
    val type: String,
    @SerializedName("occurred_at")
    val occurredAt: String,
    val locale: String,
    @SerializedName("time_zone")
    val timeZone: String
)

data class AiSuggestionResponseDto(
    val id: String,
    val status: String
)

data class AiSuggestionRejectionRequest(val reason: String)

data class NotificationMetricBatchRequest(
    val metrics: List<NotificationMetricDeltaDto>
)

data class NotificationMetricDeltaDto(
    val source: String,
    @SerializedName("parser_version")
    val parserVersion: String,
    @SerializedName("metric_date")
    val metricDate: String,
    val observed: Long,
    val parsed: Long,
    @SerializedName("parse_failures")
    val parseFailures: Long,
    @SerializedName("local_duplicates")
    val localDuplicates: Long
)
