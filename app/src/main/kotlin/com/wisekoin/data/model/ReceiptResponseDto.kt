package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName

data class ReceiptResponseDto (
    @SerializedName("store_name")
    val storeName: String,

    @SerializedName("category_id")
    val categoryId: Long?,

    @SerializedName("category")
    val category: String,

    @SerializedName("amount")
    val amount: java.math.BigDecimal
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
    val transcript: String? = null
)
