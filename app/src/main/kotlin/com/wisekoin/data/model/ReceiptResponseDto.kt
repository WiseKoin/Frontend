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