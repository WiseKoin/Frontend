package com.wisekoin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.ui.components.*
import com.wisekoin.ui.theme.*

@Composable
fun OverviewScreen(
    userName: String = "Guest",
    onNavigateToAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = CandyBg,
        topBar = { WiseKoinTopBar(userName = userName, onProfileClick = onProfileClick) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Total Balance Card
            item {
                TotalBalanceCard(
                    balance = "24,500,000 VND",
                    income = "15.2M",
                    expense = "8.4M"
                )
            }

            // 2. Add Transaction Quick Button
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

            // 3. Cash Flow Chart Card
            item {
                CashFlowCard()
            }

            // 4. Recent Transactions
            item {
                RecentTransactionsSection(onSeeAll = onSeeAllTransactions)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
