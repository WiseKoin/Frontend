package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class DebtRequest(
    @SerializedName("counterpartName") val counterpartName: String,
    @SerializedName("type") val type: String,
    @SerializedName("totalAmount") val totalAmount: BigDecimal,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("note") val note: String? = null
)

data class DebtResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("counterpartName") val counterpartName: String,
    @SerializedName("type") val type: String,
    @SerializedName("totalAmount") val totalAmount: BigDecimal,
    @SerializedName("remainingAmount") val remainingAmount: BigDecimal,
    @SerializedName("paidAmount") val paidAmount: BigDecimal,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("note") val note: String? = null
)

data class DebtRepaymentRequest(
    @SerializedName("amount") val amount: BigDecimal,
    @SerializedName("walletId") val walletId: Long,
    @SerializedName("categoryId") val categoryId: Long,
    @SerializedName("note") val note: String? = null
)
