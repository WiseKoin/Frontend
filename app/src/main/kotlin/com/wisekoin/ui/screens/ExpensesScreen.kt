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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.data.model.TransactionResponseDto
import com.wisekoin.R
import com.wisekoin.ui.components.*
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.TransactionReportPeriod
import com.wisekoin.viewmodel.WalletViewModel
import com.wisekoin.viewmodel.buildAllTimeStats
import com.wisekoin.viewmodel.buildMonthlyStats
import com.wisekoin.viewmodel.isDisplayedExpense
import com.wisekoin.viewmodel.parseTransactionDate
import java.math.BigDecimal
import java.time.LocalDate

private fun getDailyCashFlow(
    transactions: List<TransactionResponseDto>,
    today: LocalDate
): List<DailyExpense> = (1..today.lengthOfMonth()).map { day ->
    val dayTransactions = transactions.filter {
        parseTransactionDate(it.transactionDate)?.toLocalDate() == today.withDayOfMonth(day)
    }
    DailyExpense(
        day = day,
        expense = dayTransactions.filter { it.isDisplayedExpense() }.sumOf { it.amount },
        income = dayTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    )
}

private fun getWeeklyCashFlow(transactions: List<TransactionResponseDto>): List<WeeklyExpense> =
    (1..7).map { dayOfWeek ->
        val dayTransactions = transactions.filter {
            parseTransactionDate(it.transactionDate)?.dayOfWeek?.value == dayOfWeek
        }
        WeeklyExpense(
            dayOfWeek = dayOfWeek,
            expense = dayTransactions.filter { it.isDisplayedExpense() }.sumOf { it.amount },
            income = dayTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        )
    }

@Composable
fun ExpensesScreen(
    walletViewModel: WalletViewModel,
    categoryViewModel: CategoryViewModel,
    isLoggedIn: Boolean = false
) {
    val state by walletViewModel.uiState.collectAsStateWithLifecycle()
    val categoryUiState by categoryViewModel.uiState.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf(TransactionReportPeriod.ALL_TIME) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            walletViewModel.loadWallets()
            categoryViewModel.loadCategories("EXPENSE", isLoggedIn)
        }
    }

    LaunchedEffect(isLoggedIn, selectedPeriod) {
        if (isLoggedIn) walletViewModel.loadTransactionReport(selectedPeriod)
    }

    val displayBalance = state.totalBalance.formatVND()

    val now = LocalDate.now()
    val reportTransactions = state.reportTransactions
    val cashFlowStats = when (selectedPeriod) {
        TransactionReportPeriod.THIS_YEAR -> buildMonthlyStats(reportTransactions, now.year)
        TransactionReportPeriod.ALL_TIME -> buildAllTimeStats(reportTransactions)
        else -> emptyList()
    }
    val dailyData = getDailyCashFlow(reportTransactions, now)
    val weeklyData = getWeeklyCashFlow(reportTransactions)
    val reportYears = reportTransactions.mapNotNull {
        parseTransactionDate(it.transactionDate)?.year
    }.distinct().sorted()
    val periodLabel = when (selectedPeriod) {
        TransactionReportPeriod.THIS_WEEK -> stringResource(R.string.this_week)
        TransactionReportPeriod.THIS_MONTH -> stringResource(R.string.this_month)
        TransactionReportPeriod.THIS_YEAR -> stringResource(R.string.this_year)
        TransactionReportPeriod.ALL_TIME -> when (reportYears.size) {
            0 -> stringResource(R.string.all_time)
            1 -> reportYears.first().toString()
            else -> "${reportYears.first()} – ${reportYears.last()}"
        }
    }

    val expensesList = reportTransactions.filter { it.isDisplayedExpense() }
        .map { tx ->
            Transaction(
                id = tx.id.toString(),
                title = tx.note ?: tx.category?.name ?: tx.goal?.name ?: "Transfer",
                category = tx.category?.name ?: tx.goal?.let { "Savings goal" } ?: "Transfer",
                amount = "-${tx.amount.formatStringVND()}đ",
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
            Text(stringResource(R.string.expenses_patterns), fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextDark)
            Text(stringResource(R.string.expenses_subtitle), fontSize = 14.sp, color = TextLight)
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

        categoryUiState.errorMessage?.let { message ->
            item {
                NetworkErrorBanner(
                    message = message,
                    onRetry = { categoryViewModel.loadCategories("EXPENSE", isLoggedIn) }
                )
            }
        }

        item {
            CashFlowCard(
                stats = if (isLoggedIn) cashFlowStats else emptyList(),
                dailyData = if (isLoggedIn && selectedPeriod == TransactionReportPeriod.THIS_MONTH) dailyData else emptyList(),
                weeklyData = if (isLoggedIn && selectedPeriod == TransactionReportPeriod.THIS_WEEK) weeklyData else emptyList(),
                periodLabel = periodLabel,
                groupStatsByYear = selectedPeriod == TransactionReportPeriod.ALL_TIME
            )
        }

        item {
            Text(
                text = stringResource(R.string.expense_history),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val timeFilters = listOf(
                    "All time" to TransactionReportPeriod.ALL_TIME,
                    "This week" to TransactionReportPeriod.THIS_WEEK,
                    "This month" to TransactionReportPeriod.THIS_MONTH,
                    "This year" to TransactionReportPeriod.THIS_YEAR
                )
                items(timeFilters.size) { index ->
                    val (label, period) = timeFilters[index]
                    val isSelected = period == selectedPeriod
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPeriod = period },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
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

        if (state.isReportLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            }
        } else if (state.reportError) {
            item {
                Text(
                    text = stringResource(R.string.transaction_report_load_failed),
                    color = PrimaryRed,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(role = Role.Button) {
                        walletViewModel.loadTransactionReport(selectedPeriod)
                    }
                )
            }
        } else if (filteredList.isEmpty()) {
            item { Text(stringResource(R.string.no_expenses_for_period), color = TextLight) }
        }

        items(filteredList, key = { it.id }) { item ->
            TransactionRowItem(item)
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
