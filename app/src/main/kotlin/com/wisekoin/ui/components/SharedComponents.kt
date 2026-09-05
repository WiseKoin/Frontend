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
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.data.model.MonthlyStatsDto
import com.wisekoin.ui.theme.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val isExpense: Boolean,
    val time: String,
    val iconBgColor: Color
)

val sampleTransactions = mutableListOf(
    Transaction("1", "Supermarket Groceries", "Dining", "-350,000đ", true, "Today, 14:30", PeachDark),
    Transaction("2", "Monthly Salary", "Income", "+15,000,000đ", false, "Yesterday", MintDark),
    Transaction("3", "Grab Ride", "Transport", "-45,000đ", true, "Yesterday", LemonDark),
    Transaction("4", "Coffee with Friends", "Entertainment", "-85,000đ", true, "20 Oct", LavenderDark),
)

fun BigDecimal.formatVND(): String {
    return "%,d VND".format(this.toLong())
}

fun Long.formatVND(): String = "%,d VND".format(this)

fun Long.formatStringVND(): String = "%,d".format(this)

fun formatAmount(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val sb = StringBuilder()
    for ((i, char) in digits.reversed().withIndex()) {
        if (i > 0 && i % 3 == 0) sb.append(',')
        sb.append(char)
    }
    return sb.reverse().toString()
}

fun formatTransactionTime(isoString: String): String {
    return try {
        val inputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val localDateTime = java.time.LocalDateTime.parse(isoString, inputFormatter)
        val dateStr = localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
        val timeStr = localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        "$dateStr, $timeStr"
    } catch (e: Exception) {
        isoString
    }
}

fun formatTransactionDate(dateMillis: Long?, hour: Int, minute: Int): String {
    val base = if (dateMillis != null) {
        LocalDate.ofEpochDay(dateMillis / 86400000)
    } else {
        LocalDate.now()
    }
    return base.atTime(hour, minute, 0)
        .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

@Composable
fun WiseKoinTopBar(
    userName: String = "Guest",
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
                        text = if (userName == "Guest") "Hello," else "Welcome back,",
                        fontSize = 12.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = userName,
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
                .neomorphicBox(cornerRadius = 22.dp)
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

// Top Bar: Logo + Name + Profile Button + Notifications
@Composable
fun AppTopBar(
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Tên WiseKoin
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrandGreenDark)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WiseKoin",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BrandGreenDark
            )
        }

        // Cụm icon góc phải: Chuông + Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .neomorphicCircle(
                        shadowColor = Color.Black.copy(alpha = 0.08f),
                        lightColor = Color.White,
                        offset = 2.dp,
                        blur = 4.dp
                    )
                    .clip(CircleShape)
                    .background(CandyBg)
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notification",
                    tint = TextDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MintDark.copy(alpha = 0.5f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = BrandGreenDark,
                    modifier = Modifier.size(22.dp)
                )
            }
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
            .neomorphicBox(cornerRadius = 28.dp),
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
fun CashFlowCard(stats: List<MonthlyStatsDto> = emptyList()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp),
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
                    text = "This Year",
                    fontSize = 12.sp,
                    color = TextLight,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (stats.isNotEmpty()) {
                val maxExpense = stats.maxOfOrNull { it.totalExpense ?: BigDecimal.ZERO } ?: BigDecimal.ONE
                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    stats.forEachIndexed { idx, stat ->
                        val expense = stat.totalExpense ?: BigDecimal.ZERO
                        val heightRatio = if (maxExpense.compareTo(BigDecimal.ZERO) > 0) {
                            expense.divide(maxExpense, 4, java.math.RoundingMode.HALF_UP)
                                .toFloat().coerceIn(0f, 1f)
                        } else 0f
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(heightRatio)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(MintDark)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                monthNames.getOrElse(idx) { "" },
                                fontSize = 11.sp,
                                color = TextLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val heights = List(12) { 0f }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthNames.forEachIndexed { idx, month ->
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
                                    .background(MintDark)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(month, fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<Transaction> = sampleTransactions, onSeeAll: () -> Unit) {
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

        transactions.forEach { transaction ->
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
            .neomorphicBox(cornerRadius = 16.dp),
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

// -------------------------------------------------------------
// Data class đại diện cho từng tab trong bottom navigation bar
// -------------------------------------------------------------
private data class NavigationTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// -------------------------------------------------------------
// Hàm chính: WiseKoinBottomNavigation
// -------------------------------------------------------------
@Composable
fun WiseKoinBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    showFab: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 0.dp),
        color = CandyBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavContent(currentRoute, onNavigate, showFab)
        }
    }
}

// -------------------------------------------------------------
// Điều hướng nội dung hiển thị (4 tab hoặc có kèm nút FAB ở giữa)
// -------------------------------------------------------------
@Composable
private fun BottomNavContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    showFab: Boolean
) {
    val leftItems = listOf(
        NavigationTabItem("overview", "Overview", Icons.Filled.GridView),
        NavigationTabItem("expenses", "Expenses", Icons.Outlined.Payments)
    )
    val rightItems = listOf(
        NavigationTabItem("savings", "Savings", Icons.Outlined.Savings),
        NavigationTabItem("debt", "Debt", Icons.Outlined.AccountBalanceWallet)
    )

    if (showFab) {
        NavItemsGroup(leftItems, currentRoute, onNavigate)
        CenterAddFabButton { onNavigate("add_transaction") }
        NavItemsGroup(rightItems, currentRoute, onNavigate)
    } else {
        NavItemsGroup(leftItems + rightItems, currentRoute, onNavigate)
    }
}

// -------------------------------------------------------------
// Render danh sách các nút tab
// -------------------------------------------------------------
@Composable
private fun NavItemsGroup(
    items: List<NavigationTabItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    items.forEach { item ->
        BottomNavTabCell(
            item = item,
            isSelected = currentRoute.equals(item.route, ignoreCase = true),
            onClick = { onNavigate(item.route) }
        )
    }
}

// -------------------------------------------------------------
// Từng item tab đơn lẻ (hiệu ứng pill khi chọn)
// -------------------------------------------------------------
@Composable
private fun BottomNavTabCell(
    item: NavigationTabItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) Color.White else Color.Transparent)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) PrimaryRed else TextDark,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PrimaryRed else TextDark
        )
    }
}

// -------------------------------------------------------------
// Nút thêm giao dịch tròn ở giữa (khi showFab = true)
// -------------------------------------------------------------
@Composable
private fun CenterAddFabButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = PrimaryRed,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.size(46.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Transaction",
            modifier = Modifier.size(24.dp)
        )
    }
}