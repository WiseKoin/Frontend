package com.wisekoin.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MonthlyStatsDto(
    @SerializedName("month")
    val month: Int,
    @SerializedName("year")
    val year: Int,
    @SerializedName("total_income")
    val totalIncome: BigDecimal?,
    @SerializedName("total_expense")
    val totalExpense: BigDecimal?
)
