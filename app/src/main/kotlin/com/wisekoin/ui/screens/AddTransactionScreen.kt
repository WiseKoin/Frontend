package com.wisekoin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.TransactionRequest
import com.wisekoin.data.model.WalletRequest
import com.wisekoin.data.model.WalletSummaryResponseDto
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.ui.components.AppTopBar
import com.wisekoin.ui.components.formatAmount
import com.wisekoin.ui.components.formatTransactionDate
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.ReceiptViewModel
import kotlin.math.abs
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// -------------------------------------------------------------
// 1. Màn hình chính (Scaffold & State Management)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onClose: () -> Unit,
    onVoiceAdd: () -> Unit,
    onScanReceipt: () -> Unit,
    onConfirmSuccess: () -> Unit,
    onDebtCreated: () -> Unit,
    onProfileClick: () -> Unit,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
    receiptViewModel: ReceiptViewModel
) {
    var selectedType by remember { mutableStateOf("Expense") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }

    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedWalletName by remember { mutableStateOf("Loading...") }
    var walletOptions by remember { mutableStateOf<List<Pair<Long, String>>>(emptyList()) }
    var showCreateWalletDialog by remember { mutableStateOf(false) }

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableStateOf(LocalTime.now().hour) }
    var selectedMinute by remember { mutableStateOf(LocalTime.now().minute) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var counterpartName by remember { mutableStateOf("") }
    var debtDueDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDebtDueDatePicker by remember { mutableStateOf(false) }
    val debtDatePickerState = rememberDatePickerState()

    val authUiState by authViewModel.uiState.collectAsState()
    val isLoggedIn = authUiState.isLoggedIn
    val categoryUiState by categoryViewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedType, isLoggedIn) {
        if (selectedType in listOf("Expense", "Income", "Lend", "Borrow")) {
            categoryViewModel.loadCategories(selectedType, isLoggedIn)
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response: WalletSummaryResponseDto = RetrofitClient.walletApi.getWallets()
                if (response.wallets.isNotEmpty()) {
                    val first = response.wallets[0]
                    selectedWalletId = first.id
                    selectedWalletName = first.name
                    walletOptions = response.wallets.map { it.id to it.name }
                }
            } catch (e: Exception) {
                selectedWalletName = "Main Cash"
            }
        }
    }

    Column(
        modifier = Modifier
            .background(color = CandyBg)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTopBar(
            onNotificationClick = {},
            onProfileClick = onProfileClick
        )
        AmountSection(selectedType, { selectedType = it }, amount, { amount = it }, onClose)
        MiddleDynamicSection(
            type = selectedType,
            selectedCategory = category,
            selectedCategoryId = selectedCategoryId,
            onCategorySelect = { name, id ->
                category = name
                selectedCategoryId = id
            },
            isLoggedIn = isLoggedIn,
            categoryUiState = categoryUiState,
            onCreateCategory = { name, icon ->
                categoryViewModel.createCategory(
                    name = name,
                    type = selectedType,
                    icon = icon,
                    isLoggedIn = isLoggedIn,
                    onSuccess = {},
                    onError = {}
                )
            },
            counterpartName = counterpartName,
            onCounterpartNameChange = { counterpartName = it },
            debtDueDateMillis = debtDueDateMillis,
            onDebtDueDateChange = { debtDueDateMillis = it },
            showDebtDueDatePicker = showDebtDueDatePicker,
            onDebtDueDatePickerOpen = { showDebtDueDatePicker = true }
        )
        CommonTransactionFields(
            walletName = selectedWalletName,
            walletId = selectedWalletId,
            walletOptions = walletOptions,
            isLoggedIn = isLoggedIn,
            selectedDateMillis = selectedDateMillis,
            selectedHour = selectedHour,
            selectedMinute = selectedMinute,
            showDatePicker = showDatePicker,
            onDatePickerOpen = { showDatePicker = true },
            note = note,
            onNoteChange = { note = it },
            onWalletSelect = { id, name ->
                selectedWalletId = id
                selectedWalletName = name
            },
            onCreateWallet = {
                showCreateWalletDialog = true
            }
        )
        if (showCreateWalletDialog && isLoggedIn) {
            CreateWalletDialog(
                onDismiss = { showCreateWalletDialog = false },
                onWalletCreated = { newWalletId, newWalletName ->
                    showCreateWalletDialog = false
                    selectedWalletId = newWalletId
                    selectedWalletName = newWalletName
                    scope.launch {
                        try {
                            val response: WalletSummaryResponseDto = RetrofitClient.walletApi.getWallets()
                            walletOptions = response.wallets.map { it.id to it.name }
                        } catch (e: Exception) {}
                    }
                }
            )
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        if (showTimePicker) {
            CustomTimePickerDialog(
                initialHour = selectedHour,
                initialMinute = selectedMinute,
                onDismiss = { showTimePicker = false },
                onTimeSelected = { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    showTimePicker = false
                }
            )
        }
        if (showDebtDueDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDebtDueDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        debtDueDateMillis = debtDatePickerState.selectedDateMillis
                        showDebtDueDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDebtDueDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = debtDatePickerState)
            }
        }
        QuickToolActions(onVoiceAdd, onScanReceipt)
        val context = LocalContext.current
        SaveTransactionButton(
            onClick = {
                if (!isLoggedIn) {
                    Toast.makeText(context, "Please log in to save a transaction.", Toast.LENGTH_SHORT).show()
                } else if (amount.isBlank()) {
                    Toast.makeText(context, "Please enter an amount.", Toast.LENGTH_SHORT).show()
                } else if (selectedWalletId == null) {
                    Toast.makeText(context, "Please select a wallet.", Toast.LENGTH_SHORT).show()
                } else if (selectedType in listOf("Expense", "Income") && selectedCategoryId == null) {
                    Toast.makeText(context, "Please select a category.", Toast.LENGTH_SHORT).show()
                } else if (selectedType !in listOf("Expense", "Income")) {
                    val parsedAmount = amount.replace(",", "").toBigDecimalOrNull()
                    if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
                        Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                    } else if (counterpartName.isBlank()) {
                        Toast.makeText(context, "Please enter a name for the other party.", Toast.LENGTH_SHORT).show()
                    } else {
                        val debtType = if (selectedType == "Lend") "LEND" else "BORROW"
                        val dueDate = debtDueDateMillis?.let {
                            LocalDate.ofEpochDay(it / 86400000).toString()
                        }
                        scope.launch {
                            try {
                                RetrofitClient.debtApi.createDebt(
                                    DebtRequest(
                                        counterpartName = counterpartName.trim(),
                                        type = debtType,
                                        totalAmount = parsedAmount,
                                        dueDate = dueDate,
                                        note = if (note.isBlank()) null else note
                                    )
                                )
                                onDebtCreated()
                            } catch (e: Exception) {
                                Toast.makeText(context, e.localizedMessage ?: "Failed to save debt.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    val parsedAmount = amount.replace(",", "").toBigDecimalOrNull()
                    if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
                        Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            try {
                                val backendType = when (selectedType) {
                                    "Income" -> "INCOME"
                                    else -> "EXPENSE"
                                }
                                RetrofitClient.transactionApi.createTransaction(
                                    TransactionRequest(
                                        walletId = selectedWalletId!!,
                                        categoryId = selectedCategoryId!!,
                                        amount = parsedAmount,
                                        type = backendType,
                                        note = if (note.isBlank()) null else note,
                                        transactionDate = formatTransactionDate(selectedDateMillis, selectedHour, selectedMinute)
                                    )
                                )
                                onConfirmSuccess()
                            } catch (e: Exception) {
                                val errorMsg = if (e.message?.contains("Insufficient", ignoreCase = true) == true) {
                                    "Insufficient wallet balance."
                                } else {
                                    e.localizedMessage ?: "Failed to save transaction."
                                }
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        )
        Spacer(Modifier.height(8.dp))
    }
}

// -------------------------------------------------------------
// 2. Cụm điều khiển loại giao dịch & Số tiền
// -------------------------------------------------------------
@Composable
private fun AmountSection(
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
            Text("Amount (VND)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
            Spacer(Modifier.height(10.dp))
            AmountInputDisplay(amount, onAmountChange, activeColor)
        }
    }
}

@Composable
private fun AmountHeader(
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
        CircularIconButton(Icons.Default.MoreVert, "More", onClick = {})
    }
}

@Composable
private fun TypeSegmentedSwitcher(
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
                    .clickable { onTypeSelect(item) }
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
private fun AmountInputDisplay(
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
private fun MiddleDynamicSection(
    type: String,
    selectedCategory: String,
    selectedCategoryId: Long?,
    onCategorySelect: (String, Long?) -> Unit,
    isLoggedIn: Boolean,
    categoryUiState: com.wisekoin.viewmodel.CategoryUiState,
    onCreateCategory: (String, String) -> Unit,
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
            onCreateCategory = onCreateCategory
        )
    }
}

@Composable
private fun LendBorrowFields(
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
            label = { Text("Counterparty Name") },
            placeholder = { Text("e.g. John Doe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neomorphicBox(cornerRadius = 22.dp)
                .background(CandyBg, RoundedCornerShape(22.dp))
                .clickable { onDatePickerOpen() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Due Date", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
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
                Icon(Icons.Default.DateRange, contentDescription = "Pick date", tint = TextDark)
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Lưới danh mục (Category Grid) từ Backend
// -------------------------------------------------------------
private fun getIconForCategory(iconName: String?): ImageVector {
    return when (iconName?.lowercase()) {
        "restaurant", "dining" -> Icons.Outlined.Restaurant
        "transport", "car" -> Icons.Outlined.DirectionsCar
        "shopping", "bag" -> Icons.Outlined.ShoppingBag
        "coffee", "cafe" -> Icons.Outlined.LocalCafe
        "education", "school" -> Icons.Outlined.School
        "health" -> Icons.Outlined.HealthAndSafety
        "home" -> Icons.Outlined.Home
        "work", "salary" -> Icons.Outlined.Work
        "money", "income" -> Icons.Outlined.AttachMoney
        "savings" -> Icons.Outlined.Savings
        else -> Icons.Outlined.Category
    }
}

@Composable
private fun CategoryCard(
    type: String,
    selectedCategory: String,
    selectedCategoryId: Long?,
    onCategorySelect: (String, Long?) -> Unit,
    isLoggedIn: Boolean,
    categoryUiState: com.wisekoin.viewmodel.CategoryUiState,
    onCreateCategory: (String, String) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

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
                Text("Category ($type)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)

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
                        contentDescription = "Add Category",
                        modifier = Modifier.size(18.dp),
                        tint = if (isLoggedIn) BrandGreenDark else TextLight
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "New Category",
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
                        text = "Empty list (Guest mode)",
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
                        text = "Empty list. Create a new category!",
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
                                    onClick = { onCategorySelect(cat.name, cat.id) }
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
}

@Composable
private fun CategoryBackendItemCell(
    name: String,
    id: Long,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
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
            .clickable { onClick() }
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
        title = { Text("Create a new category of ${type.lowercase()}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Choose icon:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
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
                                .clickable { selectedIconName = iconName }
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
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// 5. Các trường thông tin chung (Wallet, Date, Note)
// -------------------------------------------------------------
@Composable
private fun CommonTransactionFields(
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
                        .clickable {
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
                    Text("Add New Wallet", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = BrandGreenDark)
                }
                Spacer(Modifier.height(4.dp))
            }
            walletOptions.forEach { (id, name) ->
                if (id != walletId) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .clickable {
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
            .clickable { onClick() }
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
private fun TrailingRowButton(trailingIcon: ImageVector?) {
    if (trailingIcon != null) {
        Icon(trailingIcon, null, tint = TextDark, modifier = Modifier.size(22.dp))
    } else {
        CircularIconButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Navigate", size = 32.dp, iconSize = 18.dp)
    }
}

@Composable
private fun NoteInputBox(note: String, onNoteChange: (String) -> Unit) {
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
                    if (note.isEmpty()) Text("Note (optional)...", fontSize = 14.sp, color = TextLight)
                    inner()
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 6. Dialog tạo ví mới
// -------------------------------------------------------------

private fun formatDateTime(dateMillis: Long?, hour: Int, minute: Int): String {
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
private fun FormatDateTime(dateMillis: Long?, hour: Int, minute: Int): String {
    return formatDateTime(dateMillis, hour, minute)
}

@Composable
private fun CreateWalletDialog(
    onDismiss: () -> Unit,
    onWalletCreated: (Long, String) -> Unit
) {
    var walletName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("CASH") }
    var initialBalance by remember { mutableStateOf("") }
    val types = listOf("CASH", "BANK", "E_WALLET", "CREDIT_CARD")
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Wallet", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    label = { Text("Wallet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Wallet Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
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
                                .clickable { selectedType = type }
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
                    label = { Text("Initial Balance (VND)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (walletName.isBlank()) {
                        Toast.makeText(context, "Please enter a wallet name.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val balance = initialBalance.replace(",", "").toBigDecimalOrNull() ?: BigDecimal.ZERO
                    isLoading = true
                    scope.launch {
                        try {
                            RetrofitClient.walletApi.createWallet(
                                WalletRequest(
                                    name = walletName.trim(),
                                    walletType = selectedType,
                                    initialBalance = balance
                                )
                            )
                            val response: WalletSummaryResponseDto = RetrofitClient.walletApi.getWallets()
                            val newWallet = response.wallets.last()
                            onWalletCreated(newWallet.id, newWallet.name)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                e.localizedMessage ?: "Failed to create wallet.",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenDark)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// 6. Cụm thao tác nhanh (Voice/Scan) & Nút Lưu
// -------------------------------------------------------------
@Composable
private fun QuickToolActions(onVoiceAdd: () -> Unit, onScanReceipt: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        QuickToolButton(Modifier.weight(1f), Icons.Outlined.Mic, "Voice Add", onVoiceAdd)
        QuickToolButton(Modifier.weight(1f), Icons.Outlined.ReceiptLong, "Scan Receipt", onScanReceipt)
    }
}

@Composable
private fun QuickToolButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .neomorphicBox(cornerRadius = 18.dp, elevation = 4.dp)
            .background(CandyBg, RoundedCornerShape(18.dp))
            .clickable { onClick() }
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
private fun SaveTransactionButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text("Save Transaction", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
        }
    }
}

// -------------------------------------------------------------
// 7. Reusable Button & Bottom Bar
// -------------------------------------------------------------
@Composable
private fun CircularIconButton(
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = TextDark, modifier = Modifier.size(iconSize))
    }
}

// -------------------------------------------------------------
// Custom Time Picker Dialog
// -------------------------------------------------------------
@Composable
private fun CustomTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time", fontWeight = FontWeight.Bold) },
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
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollableTimeColumn(
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