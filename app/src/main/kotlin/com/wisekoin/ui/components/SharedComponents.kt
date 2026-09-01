package com.wisekoin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.ui.theme.*

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val isExpense: Boolean,
    val time: String,
    val iconBgColor: Color
)

val sampleTransactions = listOf(
    Transaction("1", "Supermarket Groceries", "Dining", "-350,000đ", true, "Today, 14:30", PeachDark),
    Transaction("2", "Monthly Salary", "Income", "+15,000,000đ", false, "Yesterday", MintDark),
    Transaction("3", "Grab Ride", "Transport", "-45,000đ", true, "Yesterday", LemonDark),
    Transaction("4", "Coffee with Friends", "Entertainment", "-85,000đ", true, "20 Oct", LavenderDark),
)

@Composable
fun WiseKoinTopBar(
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(CircleShape).clickable { onProfileClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Welcome back,",
                        fontSize = 12.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Alex Johnson",
                        fontSize = 18.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .neomorphic(cornerRadius = 22.dp)
                .clip(CircleShape)
                .background(CandyBg)
                .clickable { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = TextDark
            )
        }
    }
}

@Composable
fun TotalBalanceCard(
    balance: String = "24,500,000 VND",
    income: String = "15.2M",
    expense: String = "8.4M"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphic(cornerRadius = 28.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Total Balance",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = balance,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MintDark.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Income", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Medium)
                        Text(income, fontSize = 15.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    }
                }

                // Expense
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Expense", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Medium)
                        Text(expense, fontSize = 15.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CashFlowCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphic(cornerRadius = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cash Flow",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "This Month",
                    fontSize = 12.sp,
                    color = TextLight,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart Mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val heights = listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.6f, 0.8f, 0.5f)

                days.forEachIndexed { idx, day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(heights[idx])
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (idx % 2 == 0) PrimaryRed else MintDark)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(day, fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(onSeeAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            TextButton(onClick = onSeeAll) {
                Text("See All", color = PrimaryRed, fontWeight = FontWeight.Bold)
            }
        }

        sampleTransactions.forEach { transaction ->
            TransactionRowItem(transaction)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun TransactionRowItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphic(cornerRadius = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(transaction.iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (transaction.isExpense) Icons.Default.ShoppingCart else Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        transaction.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        "${transaction.category} • ${transaction.time}",
                        fontSize = 12.sp,
                        color = TextLight
                    )
                }
            }
            Text(
                transaction.amount,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isExpense) TextDark else MintDark
            )
        }
    }
}

@Composable
fun WiseKoinBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphic(cornerRadius = 0.dp),
        color = CandyBg,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationBarItem(
                selected = currentRoute == "overview",
                onClick = { onNavigate("overview") },
                icon = { Icon(Icons.Outlined.Home, contentDescription = "Overview") },
                label = { Text("Overview", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryRed,
                    selectedTextColor = PrimaryRed,
                    unselectedIconColor = TextLight,
                    unselectedTextColor = TextLight,
                    indicatorColor = CandyBg
                )
            )

            NavigationBarItem(
                selected = currentRoute == "expenses",
                onClick = { onNavigate("expenses") },
                icon = { Icon(Icons.Outlined.PieChart, contentDescription = "Expenses") },
                label = { Text("Expenses", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryRed,
                    selectedTextColor = PrimaryRed,
                    unselectedIconColor = TextLight,
                    unselectedTextColor = TextLight,
                    indicatorColor = CandyBg
                )
            )

            FloatingActionButton(
                onClick = { onNavigate("add_transaction") },
                containerColor = PrimaryRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }

            NavigationBarItem(
                selected = currentRoute == "savings",
                onClick = { onNavigate("savings") },
                icon = { Icon(Icons.Outlined.Savings, contentDescription = "Savings") },
                label = { Text("Savings", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryRed,
                    selectedTextColor = PrimaryRed,
                    unselectedIconColor = TextLight,
                    unselectedTextColor = TextLight,
                    indicatorColor = CandyBg
                )
            )

            NavigationBarItem(
                selected = currentRoute == "debt",
                onClick = { onNavigate("debt") },
                icon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = "Debt") },
                label = { Text("Debt", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryRed,
                    selectedTextColor = PrimaryRed,
                    unselectedIconColor = TextLight,
                    unselectedTextColor = TextLight,
                    indicatorColor = CandyBg
                )
            )
        }
    }
}

@Composable
fun WiseKoinBottomBar(
    currentRoute: String,
    onTabClick: (String) -> Unit
) {
    WiseKoinBottomNavigation(
        currentRoute = currentRoute,
        onNavigate = onTabClick
    )
}
