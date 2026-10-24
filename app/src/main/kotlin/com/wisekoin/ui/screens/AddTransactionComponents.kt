package com.wisekoin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.R
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.model.WalletRequest
import com.wisekoin.ui.components.AppTopBar
import com.wisekoin.ui.components.NetworkErrorBanner
import com.wisekoin.ui.components.formatAmount
import com.wisekoin.ui.components.formatTransactionDate
import com.wisekoin.ui.theme.*
import com.wisekoin.util.MoneyFormatter
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.viewmodel.TransactionEntryViewModel
import kotlin.math.abs
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// -------------------------------------------------------------
// 1. Màn hình chính (Scaffold & State Management)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AmountSection(
    type: String,
    onTypeSelect: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val isDebt = type in listOf("Lend", "Borrow")
    val activeColor = if (isDebt) PrimaryRed else BrandGreenDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 28.dp)
            .background(CandyBg, RoundedCornerShape(28.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AmountHeader(type, onTypeSelect, activeColor, onClose)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.amount_vnd), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
            Spacer(Modifier.height(10.dp))
            AmountInputDisplay(amount, onAmountChange, activeColor)
        }
    }
}

@Composable
internal fun AmountHeader(
    type: String,
    onTypeSelect: (String) -> Unit,
    activeColor: Color,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircularIconButton(Icons.Default.Close, "Close", onClick = onClose)
        TypeSegmentedSwitcher(
            selectedType = type,
            activeColor = activeColor,
            onTypeSelect = onTypeSelect,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
    }
}

@Composable
internal fun TypeSegmentedSwitcher(
    selectedType: String,
    activeColor: Color,
    onTypeSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .neomorphicInset(cornerRadius = 24.dp)
            .padding(3.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Expense", "Income", "Lend", "Borrow").forEach { item ->
            val isSelected = selectedType == item
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) activeColor else Color.Transparent)
                    .minimumInteractiveComponentSize()
                    .clickable(role = Role.RadioButton) { onTypeSelect(item) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextDark
                )
            }
        }
    }
}

@Composable
internal fun AmountInputDisplay(
    amount: String,
    onAmountChange: (String) -> Unit,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicInset(cornerRadius = 24.dp, elevation = 4.dp)
            .padding(vertical = 12.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("₫", color = accentColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = amount,
                onValueChange = { newValue -> onAmountChange(formatAmount(newValue)) },
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    textAlign = TextAlign.Start
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(accentColor)
            )
        }
    }
}

// -------------------------------------------------------------
// 3. Phần thân động (Category vs Lend/Borrow Info)
// -------------------------------------------------------------
@Composable
internal fun MiddleDynamicSection(
    type: String,
    selectedCategory: String,
    selectedCategoryId: Long?,
    onCategorySelect: (String, Long?) -> Unit,
    isLoggedIn: Boolean,
    categoryUiState: com.wisekoin.viewmodel.CategoryUiState,
    onCreateCategory: (String, String) -> Unit,
    onUpdateCategory: (CategoryResponse, String, String, () -> Unit) -> Unit,
    onDeleteCategory: (CategoryResponse, () -> Unit) -> Unit,
    counterpartName: String,
    onCounterpartNameChange: (String) -> Unit,
    debtDueDateMillis: Long?,
    onDebtDueDateChange: (Long?) -> Unit,
    showDebtDueDatePicker: Boolean,
    onDebtDueDatePickerOpen: () -> Unit
) {
    if (type in listOf("Lend", "Borrow")) {
        LendBorrowFields(
            counterpartName = counterpartName,
            onCounterpartNameChange = onCounterpartNameChange,
            dueDateMillis = debtDueDateMillis,
            onDueDateChange = onDebtDueDateChange,
            showDatePicker = showDebtDueDatePicker,
            onDatePickerOpen = onDebtDueDatePickerOpen
        )
    } else {
        CategoryCard(
            type = type,
            selectedCategory = selectedCategory,
            selectedCategoryId = selectedCategoryId,
            onCategorySelect = onCategorySelect,
            isLoggedIn = isLoggedIn,
            categoryUiState = categoryUiState,
            onCreateCategory = onCreateCategory,
            onUpdateCategory = onUpdateCategory,
            onDeleteCategory = onDeleteCategory
        )
    }
}

@Composable
internal fun LendBorrowFields(
    counterpartName: String,
    onCounterpartNameChange: (String) -> Unit,
    dueDateMillis: Long?,
    onDueDateChange: (Long?) -> Unit,
    showDatePicker: Boolean,
    onDatePickerOpen: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = counterpartName,
            onValueChange = onCounterpartNameChange,
            label = { Text(stringResource(R.string.counterparty_name)) },
            placeholder = { Text(stringResource(R.string.counterparty_example)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neomorphicBox(cornerRadius = 22.dp)
                .background(CandyBg, RoundedCornerShape(22.dp))
                .clickable(role = Role.Button) { onDatePickerOpen() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.due_date), fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
                    val dateText = dueDateMillis?.let {
                        try {
                            LocalDate.ofEpochDay(it / 86400000)
                                .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        } catch (e: Exception) {
                            "No deadline (optional)"
                        }
                    } ?: "No deadline (optional)"
                    Text(dateText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.pick_date), tint = TextDark)
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Lưới danh mục (Category Grid) từ Backend
// -------------------------------------------------------------
internal fun getIconForCategory(iconName: String?): ImageVector {
    return when (iconName?.lowercase()) {
        "restaurant", "dining" -> Icons.Outlined.Restaurant
        "transport", "car" -> Icons.Outlined.DirectionsCar
        "shopping", "bag" -> Icons.Outlined.ShoppingBag
        "coffee", "cafe" -> Icons.Outlined.LocalCafe
        "education", "school" -> Icons.Outlined.School
        "health" -> Icons.Outlined.HealthAndSafety
        "home" -> Icons.Outlined.Home
        "work", "salary" -> Icons.Outlined.Work
        "money", "income", "lend" -> Icons.Outlined.AttachMoney
        "savings", "borrow" -> Icons.Outlined.Savings
        else -> Icons.Outlined.Category
    }
}

@Composable
internal fun CategoryCard(
    type: String,
    selectedCategory: String,
    selectedCategoryId: Long?,
    onCategorySelect: (String, Long?) -> Unit,
    isLoggedIn: Boolean,
    categoryUiState: com.wisekoin.viewmodel.CategoryUiState,
    onCreateCategory: (String, String) -> Unit,
    onUpdateCategory: (CategoryResponse, String, String, () -> Unit) -> Unit,
    onDeleteCategory: (CategoryResponse, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToManage by remember { mutableStateOf<CategoryResponse?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 28.dp)
            .background(CandyBg, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.category_with_type, type), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)

                TextButton(
                    onClick = {
                        if (!isLoggedIn) {
                            Toast.makeText(
                                context,
                                "Log in to add category.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showAddDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isLoggedIn) Icons.Default.Add else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isLoggedIn) BrandGreenDark else TextLight
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.new_category),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLoggedIn) BrandGreenDark else TextLight
                    )
                }
            }

            if (categoryUiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandGreenDark)
                }
            } else if (!isLoggedIn) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.guest_categories_empty),
                        fontSize = 13.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (categoryUiState.categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.categories_empty),
                        fontSize = 13.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val categories = categoryUiState.categories
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            rowItems.forEach { cat ->
                                CategoryBackendItemCell(
                                    name = cat.name,
                                    id = cat.id,
                                    icon = getIconForCategory(cat.icon),
                                    isSelected = selectedCategoryId == cat.id,
                                    onClick = { onCategorySelect(cat.name, cat.id) },
                                    onLongClick = { categoryToManage = cat }
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateCategoryDialog(
            type = type,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, icon ->
                onCreateCategory(name, icon)
                showAddDialog = false
            }
        )
    }

    categoryToManage?.let { category ->
        CategoryManagementDialog(
            category = category,
            isSubmitting = categoryUiState.mutatingCategoryId == category.id,
            errorMessage = categoryUiState.errorMessage,
            onDismiss = { categoryToManage = null },
            onSave = { name, icon ->
                onUpdateCategory(category, name, icon) {
                    if (selectedCategoryId == category.id) {
                        onCategorySelect(name, category.id)
                    }
                    categoryToManage = null
                }
            },
            onDelete = {
                onDeleteCategory(category) {
                    if (selectedCategoryId == category.id) onCategorySelect("", null)
                    categoryToManage = null
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CategoryBackendItemCell(
    name: String,
    id: Long,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val bgModifier = if (isSelected) {
        Modifier.clip(RoundedCornerShape(18.dp)).background(BrandGreenDark)
    } else {
        Modifier.neomorphicBox(cornerRadius = 18.dp, elevation = 4.dp).background(CandyBg, RoundedCornerShape(18.dp))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .minimumInteractiveComponentSize()
            .combinedClickable(role = Role.RadioButton, onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(54.dp).then(bgModifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) Color.White else TextDark,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BrandGreenDark else TextLight,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun CreateCategoryDialog(
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("restaurant") }

    val availableIcons = listOf(
        "restaurant", "transport", "shopping", "coffee",
        "education", "health", "home", "work", "money", "savings"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_category_for_type, type.lowercase()), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text(stringResource(R.string.category_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.choose_icon_label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableIcons.forEach { iconName ->
                        val isSelected = selectedIconName == iconName
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandGreenDark else CandyBg)
                                .minimumInteractiveComponentSize()
                                .clickable(role = Role.RadioButton) { selectedIconName = iconName }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForCategory(iconName),
                                contentDescription = iconName,
                                tint = if (isSelected) Color.White else TextDark
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName.trim(), selectedIconName)
                    }
                },
                enabled = categoryName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenDark)
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// -------------------------------------------------------------
// 5. Các trường thông tin chung (Wallet, Date, Note)
// -------------------------------------------------------------
@Composable
internal fun CommonTransactionFields(
    walletName: String,
    walletId: Long?,
    walletOptions: List<Pair<Long, String>>,
    isLoggedIn: Boolean,
    selectedDateMillis: Long?,
    selectedHour: Int,
    selectedMinute: Int,
    showDatePicker: Boolean,
    onDatePickerOpen: () -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onWalletSelect: (Long, String) -> Unit,
    onCreateWallet: () -> Unit
) {
    var showWalletPicker by remember { mutableStateOf(false) }
    SelectorItemRow(
        icon = Icons.Outlined.AccountBalanceWallet,
        iconBgColor = MintDark.copy(alpha = 0.35f),
        iconColor = BrandGreenDark,
        title = "Wallet",
        subtitle = walletName,
        trailingIcon = Icons.Outlined.KeyboardArrowDown,
        onClick = {
            if (walletOptions.size > 1 || isLoggedIn) showWalletPicker = !showWalletPicker
        }
    )
    if (showWalletPicker && (walletOptions.size > 1 || isLoggedIn)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (isLoggedIn) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickable(role = Role.Button) {
                            showWalletPicker = false
                            onCreateWallet()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandGreenDark.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = BrandGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.add_new_wallet), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = BrandGreenDark)
                }
                Spacer(Modifier.height(4.dp))
            }
            walletOptions.forEach { (id, name) ->
                if (id != walletId) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .clickable(role = Role.RadioButton) {
                                onWalletSelect(id, name)
                                showWalletPicker = false
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MintDark.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AccountBalanceWallet,
                                null,
                                tint = BrandGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                    }
                }
            }
        }
    }
    SelectorItemRow(
        icon = Icons.Outlined.CalendarToday,
        iconBgColor = LavenderDark.copy(alpha = 0.2f),
        iconColor = LavenderDark,
        title = "Date & Time",
        subtitle = formatDateTime(selectedDateMillis, selectedHour, selectedMinute),
        trailingIcon = Icons.Outlined.AccessTime,
        onClick = onDatePickerOpen
    )
    NoteInputBox(note, onNoteChange)
}

@Composable
fun SelectorItemRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 22.dp)
            .background(CandyBg, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
                    Text(subtitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
            TrailingRowButton(trailingIcon)
        }
    }
}

@Composable
internal fun TrailingRowButton(trailingIcon: ImageVector?) {
    if (trailingIcon != null) {
        Icon(trailingIcon, null, tint = TextDark, modifier = Modifier.size(22.dp))
    } else {
        CircularIconButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Navigate", size = 32.dp, iconSize = 18.dp)
    }
}

@Composable
internal fun NoteInputBox(note: String, onNoteChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicInset(cornerRadius = 22.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(CandyBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.EditNote, "Note", tint = LavenderDark, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = TextDark),
                decorationBox = { inner ->
                    if (note.isEmpty()) Text(stringResource(R.string.note_optional_hint), fontSize = 14.sp, color = TextLight)
                    inner()
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 6. Dialog tạo ví mới
// -------------------------------------------------------------

internal fun formatDateTime(dateMillis: Long?, hour: Int, minute: Int): String {
    val dateStr = if (dateMillis != null) {
        val date = LocalDate.ofEpochDay(dateMillis / 86400000)
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } else {
        val today = LocalDate.now()
        today.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
    return "$dateStr, ${"%02d:%02d".format(hour, minute)}"
}

@Composable
internal fun FormatDateTime(dateMillis: Long?, hour: Int, minute: Int): String {
    return formatDateTime(dateMillis, hour, minute)
}

@Composable
internal fun CreateWalletDialog(
    transactionEntryViewModel: TransactionEntryViewModel,
    onDismiss: () -> Unit,
    onWalletCreated: (Long, String) -> Unit
) {
    var walletName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("CASH") }
    var initialBalance by remember { mutableStateOf("") }
    val types = listOf("CASH", "BANK", "E_WALLET", "CREDIT_CARD")
    val submissionState by transactionEntryViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_new_wallet), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    label = { Text(stringResource(R.string.wallet_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.wallet_type), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandGreenDark else CandyBg)
                                .minimumInteractiveComponentSize()
                                .clickable(role = Role.RadioButton) { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = initialBalance,
                    onValueChange = { initialBalance = it },
                    label = { Text(stringResource(R.string.initial_balance_vnd_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (submissionState.isSubmitting) return@Button
                    if (walletName.isBlank()) {
                        Toast.makeText(context, "Please enter a wallet name.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val balance = MoneyFormatter.parse(initialBalance) ?: BigDecimal.ZERO
                    transactionEntryViewModel.createWallet(
                        request = WalletRequest(
                            name = walletName.trim(),
                            walletType = selectedType,
                            initialBalance = balance
                        ),
                        onSuccess = { newWallet ->
                            onWalletCreated(newWallet.id, newWallet.name)
                        },
                        onFailure = { message ->
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                enabled = !submissionState.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenDark)
            ) {
                if (submissionState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text(stringResource(R.string.create))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// -------------------------------------------------------------
// 6. Cụm thao tác nhanh (Voice/Scan) & Nút Lưu
// -------------------------------------------------------------
@Composable
internal fun QuickToolActions(onVoiceAdd: () -> Unit, onScanReceipt: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        QuickToolButton(Modifier.weight(1f), Icons.Outlined.Mic, "Voice Add", onVoiceAdd)
        QuickToolButton(Modifier.weight(1f), Icons.Outlined.ReceiptLong, "Scan Receipt", onScanReceipt)
    }
}

@Composable
internal fun QuickToolButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .neomorphicBox(cornerRadius = 18.dp, elevation = 4.dp)
            .background(CandyBg, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button) { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = BrandGreenDark, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

@Composable
internal fun SaveTransactionButton(onClick: () -> Unit, isSubmitting: Boolean) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        enabled = !isSubmitting
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
        } else Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.save_transaction), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
        }
    }
}

// -------------------------------------------------------------
// 7. Reusable Button & Bottom Bar
// -------------------------------------------------------------
@Composable
internal fun CircularIconButton(
    icon: ImageVector,
    description: String,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(size)
            .neomorphicCircle(shadowColor = Color.Black.copy(alpha = 0.08f), lightColor = Color.White, offset = 2.dp, blur = 4.dp)
            .clip(CircleShape)
            .background(CandyBg)
            .clickable(role = Role.Button) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = TextDark, modifier = Modifier.size(iconSize))
    }
}

// -------------------------------------------------------------
// Custom Time Picker Dialog
// -------------------------------------------------------------
@Composable
internal fun CustomTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_time), fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScrollableTimeColumn(
                    values = (0..23).toList(),
                    initial = hour,
                    label = "Hour",
                    color = BrandGreenDark
                ) { hour = it }
                Text(":", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextDark)
                ScrollableTimeColumn(
                    values = (0..59).toList(),
                    initial = minute,
                    label = "Min",
                    color = BrandGreenDark
                ) { minute = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(hour, minute) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenDark)
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ScrollableTimeColumn(
    values: List<Int>,
    initial: Int,
    label: String,
    color: Color,
    onSelected: (Int) -> Unit
) {
    val itemHeight = 36.dp
    val itemSpacing = 2.dp
    val viewportHeight = 140.dp
    val verticalPadding = (viewportHeight - itemHeight) / 2

    // Tìm index khởi tạo
    val initialIndex = values.indexOf(initial).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Lấy item nằm gần tâm (center) của viewport nhất
    val centeredItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf initialIndex

            // Tọa độ y của tâm viewport tính từ mép trên nội dung
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

            // Tìm item có tâm gần tâm viewport nhất
            visibleItems.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - viewportCenter)
            }?.index ?: initialIndex
        }
    }

    // Khi item ở giữa thay đổi (người dùng cuộn dừng lại), trigger callback
    LaunchedEffect(centeredItemIndex) {
        if (centeredItemIndex in values.indices) {
            onSelected(values[centeredItemIndex])
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray, // Thay bằng TextLight của bạn
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior, // Tự động hít số vào chính giữa
            contentPadding = PaddingValues(vertical = verticalPadding), // Cho phép số đầu/cuối cuộn ra giữa
            modifier = Modifier
                .height(viewportHeight)
                .width(50.dp),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(values.size) { index ->
                val value = values[index]
                val isSelected = index == centeredItemIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d".format(value),
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) color else Color.Gray // Thay bằng TextLight của bạn
                    )
                }
            }
        }
    }
}
