package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisekoin.R
import com.wisekoin.data.model.WalletResponseDto
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
            val categoryLabel = tx.category?.name
                ?: tx.goal?.let { "Savings goal" }
                ?: "Transfer"
            val isExpense = tx.type != "INCOME"
            Transaction(
                id = tx.id.toString(),
                title = tx.note ?: tx.goal?.name ?: categoryLabel,
                category = categoryLabel,
                amount = if (isExpense) {
                    "-${tx.amount.toLong().formatStringVND()}"
                } else {
                    "+${tx.amount.toLong().formatStringVND()}"
                },
                isExpense = isExpense,
                time = formatTransactionTime(tx.transactionDate),
                iconBgColor = if (isExpense) PeachDark else MintDark
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
                WalletOverviewSection(
                    wallets = if (isLoggedIn) state.wallets else emptyList(),
                    isLoading = isLoggedIn && state.isLoading
                )
            }

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

@Composable
private fun WalletOverviewSection(wallets: List<WalletResponseDto>, isLoading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.wallets_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        when {
            isLoading && wallets.isEmpty() -> Box(
                modifier = Modifier.fillMaxWidth().height(92.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandGreenDark)
            }
            wallets.isEmpty() -> Text(
                text = stringResource(R.string.wallets_empty),
                fontSize = 13.sp,
                color = TextLight
            )
            else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wallets, key = { it.id }) { wallet ->
                    Card(
                        modifier = Modifier.width(190.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CandyBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = BrandGreenDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    wallet.name,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                wallet.balance.formatVND(),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(wallet.type, fontSize = 11.sp, color = TextLight)
                        }
                    }
                }
            }
        }
    }
}
