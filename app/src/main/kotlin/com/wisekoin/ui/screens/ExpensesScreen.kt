package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisekoin.ui.components.*
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.WalletViewModel

@Composable
fun ExpensesScreen(
    walletViewModel: WalletViewModel = viewModel(),
    isLoggedIn: Boolean = false,
    onAddExpense: () -> Unit
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

    val displayBalance = state.totalBalance.formatVND()

    val expensesList = state.recentTransactions
        .filter { it.type == "EXPENSE" }
        .map { tx ->
            Transaction(
                id = tx.id.toString(),
                title = tx.note ?: tx.category.name,
                category = tx.category.name,
                amount = "-${tx.amount.toLong().formatStringVND()}đ",
                isExpense = true,
                time = formatTransactionTime(tx.transactionDate),
                iconBgColor = PeachDark
            )
        }

    val categories = listOf("All", "Dining", "Transport", "Education", "Entertainment")
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredList = if (selectedCategory == "All") {
        expensesList
    } else {
        expensesList.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CandyBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Expenses & Budget", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextDark)
            Text("Track your spending against monthly budget limit.", fontSize = 14.sp, color = TextLight)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neomorphicBox(cornerRadius = 28.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CandyBg)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Current Balance", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        displayBalance,
                        color = TextDark,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monthly Budget", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("—", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.5f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MintDark,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Track your spending habits",
                        color = TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expense History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                IconButton(onClick = onAddExpense) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = PrimaryRed)
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
