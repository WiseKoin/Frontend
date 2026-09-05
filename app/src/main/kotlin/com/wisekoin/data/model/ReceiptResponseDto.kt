package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName

data class ReceiptResponseDto (
    @SerializedName("store_name")
    val storeName: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("amount")
    val amount: Double
)

data class VoiceResponseDto(
    @SerializedName("amount")
    val amount: java.math.BigDecimal,

    @SerializedName("type")
    val type: String = "EXPENSE",

    @SerializedName("category")
    val category: String = "Unknown",

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("transcript")
    val transcript: String? = null
)