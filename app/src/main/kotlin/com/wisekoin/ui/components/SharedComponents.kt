package com.wisekoin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.R
import com.wisekoin.data.model.MonthlyStatsDto
import com.wisekoin.ui.theme.*
import com.wisekoin.util.MoneyFormatter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val isExpense: Boolean,
    val time: String,
    val iconBgColor: Color
)

data class DailyExpense(
    val day: Int,
    val expense: BigDecimal,
    val income: BigDecimal = BigDecimal.ZERO
)

data class WeeklyExpense(
    val dayOfWeek: Int,
    val expense: BigDecimal,
    val income: BigDecimal = BigDecimal.ZERO
)

val LocalCurrencyCode = staticCompositionLocalOf {
    MoneyFormatter.DEFAULT_CURRENCY_CODE
}

fun BigDecimal.formatCurrency(currencyCode: String): String {
    return MoneyFormatter.format(this, currencyCode)
}

fun Long.formatCurrency(currencyCode: String): String =
    BigDecimal.valueOf(this).formatCurrency(currencyCode)

fun BigDecimal.formatCurrencyValue(currencyCode: String): String = MoneyFormatter.format(
    amount = this,
    currencyCode = currencyCode,
    includeCurrencyCode = false
)

fun formatAmount(
    raw: String,
    currencyCode: String = MoneyFormatter.DEFAULT_CURRENCY_CODE
): String {
    return MoneyFormatter.formatInput(raw, currencyCode)
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
                        contentDescription = stringResource(R.string.back),
                        tint = TextDark
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { onProfileClick() }
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
                        contentDescription = stringResource(R.string.user_avatar),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (userName == stringResource(R.string.guest)) {
                            stringResource(R.string.hello)
                        } else {
                            stringResource(R.string.welcome_back)
                        },
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
                .size(48.dp)
                .neomorphicBox(cornerRadius = 22.dp)
                .clip(CircleShape)
                .background(CandyBg)
                .clickable(role = Role.Button) { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = stringResource(R.string.notifications),
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
                text = stringResource(R.string.app_name),
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
                    .size(48.dp)
                    .neomorphicCircle(
                        shadowColor = Color.Black.copy(alpha = 0.08f),
                        lightColor = Color.White,
                        offset = 2.dp,
                        blur = 4.dp
                    )
                    .clip(CircleShape)
                    .background(CandyBg)
                    .clickable(role = Role.Button) { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = stringResource(R.string.notifications),
                    tint = TextDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MintDark.copy(alpha = 0.5f))
                    .clickable(role = Role.Button) { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile),
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
                text = stringResource(R.string.total_balance),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = balance,
                fontSize = if (balance.length > 18) 24.sp else 30.sp,
                fontWeight = FontWeight.Black,
                color = TextDark,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val stackStatistics = maxWidth < 300.dp || LocalDensity.current.fontScale >= 1.3f
                if (stackStatistics) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BalanceStatistic(
                            label = stringResource(R.string.income),
                            amount = income,
                            isIncome = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        BalanceStatistic(
                            label = stringResource(R.string.expense),
                            amount = expense,
                            isIncome = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BalanceStatistic(
                            label = stringResource(R.string.income),
                            amount = income,
                            isIncome = true,
                            modifier = Modifier.weight(1f)
                        )
                        BalanceStatistic(
                            label = stringResource(R.string.expense),
                            amount = expense,
                            isIncome = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkErrorBanner(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = TextDark,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun BalanceStatistic(
    label: String,
    amount: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isIncome) MintDark.copy(alpha = 0.2f)
                    else PrimaryRed.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIncome) {
                    Icons.AutoMirrored.Filled.TrendingUp
                } else {
                    Icons.AutoMirrored.Filled.TrendingDown
                },
                contentDescription = null,
                tint = if (isIncome) Color(0xFF2E7D32) else PrimaryRed,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Medium)
            Text(
                text = amount,
                fontSize = 14.sp,
                color = TextDark,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private data class ChartBarItem(
    val label: String,
    val income: BigDecimal,
    val expense: BigDecimal
)

@Composable
fun CashFlowCard(
    stats: List<MonthlyStatsDto> = emptyList(),
    dailyData: List<DailyExpense> = emptyList(),
    weeklyData: List<WeeklyExpense> = emptyList(),
    periodLabel: String = "This Year",
    groupStatsByYear: Boolean = false
) {
    val isMonthly = stats.isNotEmpty() && !groupStatsByYear
    val isDaily = dailyData.isNotEmpty()
    val isWeekly = weeklyData.isNotEmpty()

    // 1. Chuẩn hóa tất cả các nguồn dữ liệu về một danh sách ChartBarItem thống nhất
    val chartItems = remember(stats, dailyData, weeklyData, periodLabel) {
        when {
            isWeekly -> {
                val weekDays = DayOfWeek.entries.map {
                    it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                }
                (1..7).map { dow ->
                    val exp = weeklyData.find { it.dayOfWeek == dow }?.expense ?: BigDecimal.ZERO
                    val income = weeklyData.find { it.dayOfWeek == dow }?.income ?: BigDecimal.ZERO
                    ChartBarItem(weekDays[dow - 1], income, exp)
                }
            }
            isDaily -> {
                val daysInMonth = LocalDate.now().lengthOfMonth()
                (1..daysInMonth).map { d ->
                    val exp = dailyData.find { it.day == d }?.expense ?: BigDecimal.ZERO
                    val income = dailyData.find { it.day == d }?.income ?: BigDecimal.ZERO
                    ChartBarItem("%02d".format(d), income, exp)
                }
            }
            isMonthly -> {
                stats.map { stat ->
                    ChartBarItem(
                        label = stat.month.takeIf { it in 1..12 }?.toString().orEmpty(),
                        income = stat.totalIncome ?: BigDecimal.ZERO,
                        expense = stat.totalExpense ?: BigDecimal.ZERO
                    )
                }
            }
            groupStatsByYear -> {
                val yearGrouped = stats.groupBy { it.year }
                yearGrouped.keys.sorted().map { year ->
                    val totalIncome = yearGrouped[year]
                        ?.mapNotNull { it.totalIncome }
                        ?.fold(BigDecimal.ZERO, BigDecimal::add) ?: BigDecimal.ZERO
                    val totalExpense = yearGrouped[year]
                        ?.mapNotNull { it.totalExpense }
                        ?.fold(BigDecimal.ZERO, BigDecimal::add) ?: BigDecimal.ZERO
                    ChartBarItem(year.toString(), totalIncome, totalExpense)
                }
            }
            else -> {
                (1..12).map { month ->
                    ChartBarItem(month.toString(), BigDecimal.ZERO, BigDecimal.ZERO)
                }
            }
        }
    }

    val maxAmount = chartItems.maxOfOrNull { maxOf(it.income, it.expense) } ?: BigDecimal.ONE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cash_flow),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = periodLabel,
                    fontSize = 12.sp,
                    color = TextLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CashFlowLegend(color = MintDark, label = stringResource(R.string.income))
                Spacer(Modifier.width(14.dp))
                CashFlowLegend(color = PrimaryRed, label = stringResource(R.string.expense))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bar Chart Area
            val rowModifier = if (isDaily) {
                // Với tháng 30-31 ngày, cho phép cuộn ngang để không bị méo/tràn
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .horizontalScroll(rememberScrollState())
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            }

            Row(
                modifier = rowModifier,
                horizontalArrangement = if (isDaily) Arrangement.spacedBy(10.dp) else Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                chartItems.forEach { item ->
                    val incomeRatio = chartHeightRatio(item.income, maxAmount)
                    val expenseRatio = chartHeightRatio(item.expense, maxAmount)

                    val itemModifier = if (isDaily) {
                        Modifier.fillMaxHeight().width(32.dp)
                    } else {
                        Modifier.fillMaxHeight().weight(1f)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = itemModifier
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            CashFlowBar(incomeRatio, MintDark)
                            CashFlowBar(expenseRatio, PrimaryRed)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            color = TextLight,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CashFlowLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RowScope.CashFlowBar(heightRatio: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(8.dp)
            .fillMaxHeight(heightRatio.coerceAtLeast(0.02f))
            .clip(RoundedCornerShape(8.dp))
            .background(if (heightRatio > 0f) color else Color.Transparent)
    )
}

private fun chartHeightRatio(amount: BigDecimal, maxAmount: BigDecimal): Float =
    if (maxAmount > BigDecimal.ZERO) {
        amount.divide(maxAmount, 4, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

@Composable
fun RecentTransactionsSection(transactions: List<Transaction>, onSeeAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recent_transactions),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            TextButton(onClick = onSeeAll) {
                Text(stringResource(R.string.see_all), color = PrimaryRed, fontWeight = FontWeight.Bold)
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        transaction.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${transaction.category} • ${transaction.time}",
                        fontSize = 12.sp,
                        color = TextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                transaction.amount,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .widthIn(max = 132.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isExpense) TextDark else MintDark,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End
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
        NavigationTabItem("overview", stringResource(R.string.overview), Icons.Filled.GridView),
        NavigationTabItem("expenses", stringResource(R.string.expenses), Icons.Outlined.Payments)
    )
    val rightItems = listOf(
        NavigationTabItem("savings", stringResource(R.string.savings), Icons.Outlined.Savings),
        NavigationTabItem("debt", stringResource(R.string.debt), Icons.Outlined.AccountBalanceWallet)
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
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .semantics { selected = isSelected }
            .clickable(role = Role.Tab) { onClick() }
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
        modifier = Modifier.size(48.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_transaction),
            modifier = Modifier.size(24.dp)
        )
    }
}
