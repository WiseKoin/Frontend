package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class GoalRequestDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("targetAmount")
    val targetAmount: BigDecimal,
    @SerializedName("deadline")
    val deadline: String? = null
)

data class GoalResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("targetAmount")
    val targetAmount: BigDecimal,
    @SerializedName("currentAmount")
    val currentAmount: BigDecimal,
    @SerializedName("remainingAmount")
    val remainingAmount: BigDecimal,
    @SerializedName("deadline")
    val deadline: String? = null,
    @SerializedName("status")
    val status: String,
    @SerializedName("progressPercent")
    val progressPercent: BigDecimal
)