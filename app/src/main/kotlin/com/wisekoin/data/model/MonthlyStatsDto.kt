package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MonthlyStatsDto(
    @SerializedName("month")
    val month: Int,
    @SerializedName("year")
    val year: Int,
    @SerializedName("totalIncome")
    val totalIncome: BigDecimal?,
    @SerializedName("totalExpense")
    val totalExpense: BigDecimal?
)
