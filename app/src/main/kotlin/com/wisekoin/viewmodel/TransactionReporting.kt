package com.wisekoin.viewmodel

import com.wisekoin.data.model.MonthlyStatsDto
import com.wisekoin.data.model.TransactionResponseDto
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class TransactionReportPeriod {
    ALL_TIME,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR
}

data class TransactionDateRange(
    val startDate: LocalDate?,
    val endDate: LocalDate?
)

internal fun TransactionReportPeriod.dateRange(today: LocalDate): TransactionDateRange =
    when (this) {
        TransactionReportPeriod.ALL_TIME -> TransactionDateRange(null, null)
        TransactionReportPeriod.THIS_WEEK -> TransactionDateRange(
            today.with(DayOfWeek.MONDAY),
            today.with(DayOfWeek.SUNDAY)
        )
        TransactionReportPeriod.THIS_MONTH -> TransactionDateRange(
            today.withDayOfMonth(1),
            today.withDayOfMonth(today.lengthOfMonth())
        )
        TransactionReportPeriod.THIS_YEAR -> TransactionDateRange(
            today.withDayOfYear(1),
            today.withDayOfYear(today.lengthOfYear())
        )
    }

internal fun parseTransactionDate(value: String): LocalDateTime? =
    runCatching { LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }.getOrNull()

internal fun TransactionResponseDto.isDisplayedExpense(): Boolean =
    type == "EXPENSE" || (type == "TRANSFER" && goal != null)

internal fun buildMonthlyStats(
    transactions: List<TransactionResponseDto>,
    year: Int
): List<MonthlyStatsDto> {
    val grouped = transactions
        .mapNotNull { transaction ->
            parseTransactionDate(transaction.transactionDate)?.let { it to transaction }
        }
        .filter { (date, _) -> date.year == year }
        .groupBy { (date, _) -> date.monthValue }

    return (1..12).map { month ->
        val monthTransactions = grouped[month].orEmpty().map { it.second }
        MonthlyStatsDto(
            month = month,
            year = year,
            totalIncome = monthTransactions
                .filter { it.type == "INCOME" }
                .sumAmounts(),
            totalExpense = monthTransactions
                .filter { it.isDisplayedExpense() }
                .sumAmounts()
        )
    }
}

internal fun buildAllTimeStats(
    transactions: List<TransactionResponseDto>
): List<MonthlyStatsDto> = transactions
    .mapNotNull { parseTransactionDate(it.transactionDate)?.year }
    .distinct()
    .sorted()
    .flatMap { year -> buildMonthlyStats(transactions, year) }

private fun List<TransactionResponseDto>.sumAmounts(): BigDecimal =
    fold(BigDecimal.ZERO) { total, transaction -> total + transaction.amount }
