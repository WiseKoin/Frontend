package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AiCategoryCandidateDto(
    @SerializedName("category_id")
    val categoryId: Long,
    @SerializedName("category")
    val category: String,
    @SerializedName("rank")
    val rank: Int
)

data class AiFieldAmbiguityDto(
    @SerializedName("field")
    val field: String,
    @SerializedName("code")
    val code: String
)

data class AiDuplicateCandidateDto(
    @SerializedName("transaction_id")
    val transactionId: Long,
    @SerializedName("amount")
    val amount: BigDecimal,
    @SerializedName("type")
    val type: String,
    @SerializedName("transaction_date")
    val transactionDate: String,
    @SerializedName("match_reasons")
    val matchReasons: List<String> = emptyList()
)

data class AiConfidenceDto(
    @SerializedName("score")
    val score: BigDecimal,
    @SerializedName("level")
    val level: String,
    @SerializedName("reasons")
    val reasons: List<String> = emptyList(),
    @SerializedName("profile")
    val profile: String,
    @SerializedName("calibrated")
    val calibrated: Boolean = false
)

data class ReceiptResponseDto (
    @SerializedName("store_name")
    val storeName: String,

    @SerializedName("category_id")
    val categoryId: Long?,

    @SerializedName("category")
    val category: String,

    @SerializedName("amount")
    val amount: BigDecimal,

    @SerializedName("currency")
    val currency: String? = null,

    @SerializedName("occurred_at")
    val occurredAt: String? = null,

    @SerializedName("subtotal")
    val subtotal: BigDecimal? = null,

    @SerializedName("tax")
    val tax: BigDecimal? = null,

    @SerializedName("discount")
    val discount: BigDecimal? = null,

    @SerializedName("tip")
    val tip: BigDecimal? = null,

    @SerializedName("payment_method")
    val paymentMethod: String? = null,

    @SerializedName("category_candidates")
    val categoryCandidates: List<AiCategoryCandidateDto>? = null,

    @SerializedName("requires_clarification")
    val requiresClarification: Boolean = false,

    @SerializedName("ambiguities")
    val ambiguities: List<AiFieldAmbiguityDto>? = null,

    @SerializedName("duplicate_candidates")
    val duplicateCandidates: List<AiDuplicateCandidateDto>? = null,

    @SerializedName("extraction_id")
    val extractionId: String? = null,

    @SerializedName("prompt_version")
    val promptVersion: String? = null,

    @SerializedName("model_version")
    val modelVersion: String? = null,

    @SerializedName("provider")
    val provider: String? = null,

    @SerializedName("rule_applied")
    val ruleApplied: Boolean = false,

    @SerializedName("confidence")
    val confidence: AiConfidenceDto? = null,

    @SerializedName("request_id")
    val requestId: String? = null
)

data class VoiceTransactionDto(
    @SerializedName("amount")
    val amount: BigDecimal,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("type")
    val type: String = "UNKNOWN",

    @SerializedName("category_id")
    val categoryId: Long? = null,

    @SerializedName("category")
    val category: String = "Unknown",

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("occurred_at")
    val occurredAt: String? = null,

    @SerializedName("source_wallet_hint")
    val sourceWalletHint: String? = null,

    @SerializedName("destination_wallet_hint")
    val destinationWalletHint: String? = null,

    @SerializedName("category_candidates")
    val categoryCandidates: List<AiCategoryCandidateDto>? = null,

    @SerializedName("requires_clarification")
    val requiresClarification: Boolean = false,

    @SerializedName("ambiguities")
    val ambiguities: List<AiFieldAmbiguityDto>? = null,

    @SerializedName("duplicate_candidates")
    val duplicateCandidates: List<AiDuplicateCandidateDto>? = null,

    @SerializedName("extraction_id")
    val extractionId: String? = null,

    @SerializedName("counterparty")
    val counterparty: String? = null,

    @SerializedName("source_wallet_id")
    val sourceWalletId: Long? = null,

    @SerializedName("destination_wallet_id")
    val destinationWalletId: Long? = null,

    @SerializedName("prompt_version")
    val promptVersion: String? = null,

    @SerializedName("model_version")
    val modelVersion: String? = null,

    @SerializedName("provider")
    val provider: String? = null,

    @SerializedName("rule_applied")
    val ruleApplied: Boolean = false,

    @SerializedName("confidence")
    val confidence: AiConfidenceDto? = null
)

data class VoiceResponseDto(
    @SerializedName("amount")
    val amount: java.math.BigDecimal,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("type")
    val type: String = "UNKNOWN",

    @SerializedName("category_id")
    val categoryId: Long? = null,

    @SerializedName("category")
    val category: String = "Unknown",

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("transcript")
    val transcript: String? = null,

    @SerializedName("occurred_at")
    val occurredAt: String? = null,

    @SerializedName("source_wallet_hint")
    val sourceWalletHint: String? = null,

    @SerializedName("destination_wallet_hint")
    val destinationWalletHint: String? = null,

    @SerializedName("requires_clarification")
    val requiresClarification: Boolean = false,

    @SerializedName("transactions")
    val transactions: List<VoiceTransactionDto>? = null,

    @SerializedName("extraction_id")
    val extractionId: String? = null,

    @SerializedName("counterparty")
    val counterparty: String? = null,

    @SerializedName("source_wallet_id")
    val sourceWalletId: Long? = null,

    @SerializedName("destination_wallet_id")
    val destinationWalletId: Long? = null,

    @SerializedName("request_id")
    val requestId: String? = null
)
