package com.wisekoin.viewmodel

import com.wisekoin.data.model.GoalInfoDto
import com.wisekoin.data.model.TransactionResponseDto
import com.wisekoin.data.model.WalletInfoDto
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionReportingTest {
    @Test
    fun `ISO parser accepts backend fractional seconds`() {
        assertNotNull(parseTransactionDate("2026-10-11T22:10:24.621045"))
    }

    @Test
    fun `week range is bounded from Monday through Sunday`() {
        val range = TransactionReportPeriod.THIS_WEEK.dateRange(LocalDate.of(2026, 10, 12))

        assertEquals(LocalDate.of(2026, 10, 12), range.startDate)
        assertEquals(LocalDate.of(2026, 10, 18), range.endDate)
    }

    @Test
    fun `monthly totals include goal funding as expense and preserve income`() {
        val transactions = listOf(
            transaction(type = "INCOME", amount = "200", date = "2026-10-01T09:00:00"),
            transaction(type = "EXPENSE", amount = "40", date = "2026-10-02T09:00:00.1"),
            transaction(
                type = "TRANSFER",
                amount = "60",
                date = "2026-10-03T09:00:00.123456",
                goal = GoalInfoDto(2, "Laptop")
            ),
            transaction(type = "TRANSFER", amount = "90", date = "2026-10-04T09:00:00")
        )

        val october = buildMonthlyStats(transactions, 2026).single { it.month == 10 }

        assertEquals(BigDecimal("200"), october.totalIncome)
        assertEquals(BigDecimal("100"), october.totalExpense)
        assertTrue(transactions[2].isDisplayedExpense())
        assertTrue(!transactions[3].isDisplayedExpense())
    }

    @Test
    fun `all time stats contain only actual transaction years`() {
        val stats = buildAllTimeStats(
            listOf(
                transaction(type = "EXPENSE", amount = "1", date = "2022-01-01T00:00:00"),
                transaction(type = "INCOME", amount = "2", date = "2026-01-01T00:00:00")
            )
        )

        assertEquals(listOf(2022, 2026), stats.map { it.year }.distinct())
    }

    private fun transaction(
        type: String,
        amount: String,
        date: String,
        goal: GoalInfoDto? = null
    ) = TransactionResponseDto(
        id = date.hashCode().toLong(),
        amount = BigDecimal(amount),
        type = type,
        note = null,
        transactionDate = date,
        wallet = WalletInfoDto(1, "Cash"),
        goal = goal
    )
}
