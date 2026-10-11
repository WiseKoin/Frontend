package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisekoin.data.model.TransactionResponseDto
import com.wisekoin.ui.components.*
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.WalletViewModel
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun parseTransactionDate(isoString: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(isoString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
    } catch (e: Exception) {
        null
    }
}

private fun getExpensesByTimeFilter(
    transactions: List<TransactionResponseDto>,
    timeFilter: String
): List<TransactionResponseDto> {
    val now = LocalDateTime.now()
    return transactions
        .filter { it.type == "EXPENSE" }
        .filter { tx ->
            val dt = parseTransactionDate(tx.transactionDate)
                ?: return@filter true
            when (timeFilter) {
                "This week" -> dt >= now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay()
                "This month" -> dt >= now.toLocalDate().withDayOfMonth(1).atStartOfDay()
                "This year" -> dt >= now.toLocalDate().withDayOfYear(1).atStartOfDay()
                else -> true
            }
        }
}

private fun getDailyExpenses(transactions: List<TransactionResponseDto>): List<DailyExpense> {
    val now = LocalDate.now()
    val daysInMonth = now.lengthOfMonth()
    val map: MutableMap<Int, BigDecimal> = mutableMapOf()
    for (d in 1..daysInMonth) map[d] = BigDecimal.ZERO
    for (tx in transactions) {
        val dt = parseTransactionDate(tx.transactionDate) ?: continue
        if (dt.monthValue == now.monthValue) {
            val day = dt.dayOfMonth
            map[day] = (map[day] ?: BigDecimal.ZERO) + tx.amount
        }
    }
    return map.entries.map { DailyExpense(it.key, it.value) }
}

private fun getWeeklyExpenses(transactions: List<TransactionResponseDto>): List<WeeklyExpense> {
    val map: MutableMap<Int, BigDecimal> = mutableMapOf()
    for (d in 1..7) map[d] = BigDecimal.ZERO
    for (tx in transactions) {
        val dt = parseTransactionDate(tx.transactionDate) ?: continue
        val dow = dt.dayOfWeek.value
        map[dow] = (map[dow] ?: BigDecimal.ZERO) + tx.amount
    }
    return map.entries.map { WeeklyExpense(it.key, it.value) }
}

@Composable
fun ExpensesScreen(
    walletViewModel: WalletViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    isLoggedIn: Boolean = false
) {
    val state by walletViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            walletViewModel.refreshAll()
            categoryViewModel.loadCategories("EXPENSE", isLoggedIn)
        }
    }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            walletViewModel.refreshAll()
            categoryViewModel.loadCategories("EXPENSE", isLoggedIn)
        }
    }

    val displayBalance = state.totalBalance.formatVND()

    var selectedTimeFilter by remember { mutableStateOf("All time") }

    val now = LocalDate.now()
    val cashFlowStats = when (selectedTimeFilter) {
        "This year" -> state.monthlyStats.filter { it.year == now.year }
        else -> state.monthlyStats
    }
    val dailyData = getDailyExpenses(getExpensesByTimeFilter(state.recentTransactions, selectedTimeFilter))
    val weeklyData = getWeeklyExpenses(getExpensesByTimeFilter(state.recentTransactions, selectedTimeFilter))
    val periodLabel = when (selectedTimeFilter) {
        "This week" -> "This Week"
        "This month" -> "This Month"
        "This year" -> "This Year"
        else -> "${now.year - 10} – ${now.year}"
    }

    val expensesList = getExpensesByTimeFilter(state.recentTransactions, selectedTimeFilter)
        .map { tx ->
            Transaction(
                id = tx.id.toString(),
                title = tx.note ?: tx.category?.name ?: tx.goal?.name ?: "Transfer",
                category = tx.category?.name ?: tx.goal?.let { "Savings goal" } ?: "Transfer",
                amount = "-${tx.amount.toLong().formatStringVND()}đ",
                isExpense = true,
                time = formatTransactionTime(tx.transactionDate),
                iconBgColor = PeachDark
            )
        }

    val categories = listOf("All") + categoryUiState.categories.map { it.name }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredList = if (selectedCategory == "All") {
        expensesList
    } else {
        expensesList.filter { it.category == selectedCategory }
    }

    var showBudgetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CandyBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Expenses & Patterns", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextDark)
            Text("Track your spending in this week, month or year.", fontSize = 14.sp, color = TextLight)
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LemonDark,
                            selectedLabelColor = TextDark,
                            containerColor = CandyBg,
                            labelColor = TextLight
                        ),
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }

        item {
            CashFlowCard(
                stats = if (isLoggedIn) cashFlowStats else emptyList(),
                dailyData = if (isLoggedIn && selectedTimeFilter == "This month") dailyData else emptyList(),
                weeklyData = if (isLoggedIn && selectedTimeFilter == "This week") weeklyData else emptyList(),
                periodLabel = periodLabel
            )
        }

        item {
            Text(
                text = "Expense History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val timeFilters = listOf("All time", "This week", "This month", "This year")
                items(timeFilters.size) { index ->
                    val filter = timeFilters[index]
                    val isSelected = filter == selectedTimeFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTimeFilter = filter },
                        label = { Text(filter, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LemonDark,
                            selectedLabelColor = TextDark,
                            containerColor = CandyBg,
                            labelColor = TextLight
                        ),
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }

        items(filteredList) { item ->
            TransactionRowItem(item)
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
