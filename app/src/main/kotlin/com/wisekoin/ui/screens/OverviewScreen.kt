package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisekoin.ui.components.*
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.WalletViewModel

@Composable
fun OverviewScreen(
    userName: String = "Guest",
    isLoggedIn: Boolean = false,
    walletViewModel: WalletViewModel = viewModel(),
    onNavigateToAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val state by walletViewModel.uiState.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            walletViewModel.refreshAll()
        }
    }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            walletViewModel.refreshAll()
        }
    }

    val displayBalance = if (isLoggedIn)
        state.totalBalance.formatVND()
    else "0 VND"

    val displayIncome = if (isLoggedIn)
        state.currentMonthIncome.formatVND()
    else "0 VND"

    val displayExpense = if (isLoggedIn)
        state.currentMonthExpense.formatVND()
    else "0 VND"

    val displayTransactions = if (isLoggedIn) {
        state.recentTransactions.map { tx ->
            Transaction(
                id = tx.id.toString(),
                title = tx.note ?: tx.category.name,
                category = tx.category.name,
                amount = if (tx.type == "EXPENSE") {
                    "-${tx.amount.toLong().formatStringVND()}"
                } else {
                    "+${tx.amount.toLong().formatStringVND()}"
                },
                isExpense = tx.type == "EXPENSE",
                time = formatTransactionTime(tx.transactionDate),
                iconBgColor = if (tx.type == "EXPENSE") PeachDark else MintDark
            )
        }
    } else emptyList()

    Scaffold(
        containerColor = CandyBg,
        topBar = {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                AppTopBar(
                    onNotificationClick = {},
                    onProfileClick = onProfileClick,
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .background(color = CandyBg)
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                TotalBalanceCard(
                    balance = displayBalance,
                    income = displayIncome,
                    expense = displayExpense
                )
            }

            item {
                Button(
                    onClick = onNavigateToAddTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add Transaction",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            item {
                CashFlowCard(stats = if (isLoggedIn) state.monthlyStats else emptyList())
            }

            item {
                RecentTransactionsSection(
                    transactions = displayTransactions,
                    onSeeAll = onSeeAllTransactions
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
