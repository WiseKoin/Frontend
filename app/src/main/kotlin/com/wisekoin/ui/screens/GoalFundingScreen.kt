package com.wisekoin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.data.model.GoalResponseDto
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.ui.components.formatAmount
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.GoalViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFundingScreen(
    goalId: Long,
    onClose: () -> Unit,
    goalViewModel: GoalViewModel,
    categoryViewModel: CategoryViewModel,
    isLoggedIn: Boolean
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    var selectedWalletName by remember { mutableStateOf("Loading...") }
    var walletOptions by remember { mutableStateOf<List<Pair<Long, String>>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryName by remember { mutableStateOf("Select category") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var isCreatingCategory by remember { mutableStateOf(false) }

    val uiState by goalViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val goal = uiState.goals.find { it.id == goalId }

    LaunchedEffect(goalId) {
        if (goalViewModel.uiState.value.goals.isEmpty()) {
            goalViewModel.loadGoals()
        }
        try {
            val response = RetrofitClient.walletApi.getWallets()
            val wallets = response.wallets.map { it.id to it.name }
            walletOptions = wallets
            if (wallets.isNotEmpty()) {
                selectedWalletId = wallets.first().first
                selectedWalletName = wallets.first().second
            }
        } catch (e: Exception) {
            selectedWalletName = "No wallet available"
        }
    }

    LaunchedEffect(isLoggedIn) {
        categoryViewModel.loadCategories("Expense", isLoggedIn)
    }

    LaunchedEffect(categoryUiState.categories) {
        if (categoryUiState.categories.isNotEmpty() && selectedCategoryId == null) {
            selectedCategoryId = categoryUiState.categories[0].id
            selectedCategoryName = categoryUiState.categories[0].name
        }
    }
    Scaffold(
        containerColor = CandyBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Funds to Goal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CandyBg)
            )
        }
    ) { padding ->
        FundingFormContent(
            padding = padding,
            goal = goal,
            amount = amount,
            onAmountChange = { amount = it },
            walletName = selectedWalletName,
            walletId = selectedWalletId,
            walletOptions = walletOptions,
            onWalletSelect = { id, name ->
                selectedWalletId = id
                selectedWalletName = name
            },
            categoryName = selectedCategoryName,
            categoryId = selectedCategoryId,
            categories = categoryUiState.categories,
            categoriesLoading = categoryUiState.isLoading || isCreatingCategory,
            isLoggedIn = isLoggedIn,
            onCategorySelect = { id, name ->
                selectedCategoryId = id
                selectedCategoryName = name
            },
            onNewCategoryRequested = { showCreateCategoryDialog = true },
            note = note,
            onNoteChange = { note = it },
            isSubmitting = uiState.isFunding,
            onSubmit = {
                if (goal == null) {
                    Toast.makeText(context, "Goal not found. Please go back and select a goal.", Toast.LENGTH_SHORT).show()
                    return@FundingFormContent
                }
                if (goal.id <= 0L) {
                    Toast.makeText(context, "Invalid goal ID.", Toast.LENGTH_SHORT).show()
                    return@FundingFormContent
                }
                val parsedAmount = amount.replace(",", "").toBigDecimalOrNull()
                if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
                    Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                    return@FundingFormContent
                }
                if (selectedWalletId == null) {
                    Toast.makeText(context, "Please select a wallet.", Toast.LENGTH_SHORT).show()
                    return@FundingFormContent
                }
                if (selectedCategoryId == null) {
                    Toast.makeText(context, "Please select a category.", Toast.LENGTH_SHORT).show()
                    return@FundingFormContent
                }
                goalViewModel.fundGoal(
                    goalId = goal.id,
                    walletId = selectedWalletId!!,
                    categoryId = selectedCategoryId!!,
                    amount = parsedAmount,
                    note = if (note.isBlank()) "Fund goal - ${goal.name}" else note.trim(),
                    onSuccess = {
                        Toast.makeText(context, "Deposit successful!", Toast.LENGTH_SHORT).show()
                        onClose()
                    },
                    onError = { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showCreateCategoryDialog && isLoggedIn) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onConfirm = { name, icon ->
                isCreatingCategory = true
                categoryViewModel.createCategory(
                    name = name,
                    type = "Expense",
                    icon = icon,
                    isLoggedIn = isLoggedIn,
                    onSuccess = {
                        isCreatingCategory = false
                        showCreateCategoryDialog = false
                        val newCat = categoryViewModel.uiState.value.categories
                            .firstOrNull { it.name.lowercase() == name.trim().lowercase() }
                        if (newCat != null) {
                            selectedCategoryId = newCat.id
                            selectedCategoryName = newCat.name
                        }
                    },
                    onError = { errorMsg ->
                        isCreatingCategory = false
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

@Composable
private fun FundingFormContent(
    padding: PaddingValues,
    goal: GoalResponseDto?,
    amount: String,
    onAmountChange: (String) -> Unit,
    walletName: String,
    walletId: Long?,
    walletOptions: List<Pair<Long, String>>,
    onWalletSelect: (Long, String) -> Unit,
    categoryName: String,
    categoryId: Long?,
    categories: List<CategoryResponse>,
    categoriesLoading: Boolean,
    isLoggedIn: Boolean,
    onCategorySelect: (Long, String) -> Unit,
    onNewCategoryRequested: () -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (goal != null) {
            FundingGoalBanner(goal)
        } else {
            FundingLoadingBanner()
        }

        FundingAmountField(amount, onAmountChange)
        FundingWalletField(
            walletName = walletName,
            walletId = walletId,
            walletOptions = walletOptions,
            onWalletSelect = onWalletSelect
        )
        FundingCategoryField(
            categoryName = categoryName,
            categoryId = categoryId,
            categories = categories,
            isLoading = categoriesLoading,
            isLoggedIn = isLoggedIn,
            onCategorySelect = onCategorySelect,
            onNewCategoryRequested = onNewCategoryRequested
        )
        FundingNoteField(note, onNoteChange)
        Spacer(Modifier.height(8.dp))
        FundingSubmitButton(isSubmitting, onSubmit)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FundingLoadingBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryRed)
        }
    }
}

@Composable
private fun FundingGoalBanner(goal: GoalResponseDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                goal.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${goal.currentAmount.formatCompactVND()} / ${goal.targetAmount.formatCompactVND()} VND",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { goal.progressPercent.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${goal.progressPercent.stripTrailingZeros().toInt()}% complete",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun FundingAmountField(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Column {
        Text("Deposit Amount (VND)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neomorphicInset(cornerRadius = 24.dp, elevation = 4.dp)
                .padding(vertical = 14.dp, horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₫", color = PrimaryRed, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = amount,
                    onValueChange = { newValue -> onAmountChange(formatAmount(newValue)) },
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark,
                        textAlign = TextAlign.Start
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(PrimaryRed),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FundingWalletField(
    walletName: String,
    walletId: Long?,
    walletOptions: List<Pair<Long, String>>,
    onWalletSelect: (Long, String) -> Unit
) {
    var showWalletPicker by remember { mutableStateOf(false) }
    SelectorItemRow(
        icon = Icons.Outlined.AccountBalanceWallet,
        iconBgColor = MintDark.copy(alpha = 0.35f),
        iconColor = BrandGreenDark,
        title = "Wallet",
        subtitle = walletName,
        trailingIcon = Icons.Outlined.KeyboardArrowDown,
        onClick = { if (walletOptions.isNotEmpty()) showWalletPicker = !showWalletPicker }
    )
    if (showWalletPicker && walletOptions.isNotEmpty()) {
        walletOptions.forEach { (id, name) ->
            if (id != walletId) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onWalletSelect(id, name)
                            showWalletPicker = false
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = BrandGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                }
            }
        }
    }
}

@Composable
private fun FundingCategoryField(
    categoryName: String,
    categoryId: Long?,
    categories: List<CategoryResponse>,
    isLoading: Boolean,
    isLoggedIn: Boolean,
    onCategorySelect: (Long, String) -> Unit,
    onNewCategoryRequested: () -> Unit
) {
    var showCategoryPicker by remember { mutableStateOf(false) }
    SelectorItemRow(
        icon = Icons.Outlined.Label,
        iconBgColor = LavenderDark.copy(alpha = 0.2f),
        iconColor = LavenderDark,
        title = "Category",
        subtitle = categoryName,
        trailingIcon = Icons.Outlined.KeyboardArrowDown,
        onClick = { if (categories.isNotEmpty()) showCategoryPicker = !showCategoryPicker }
    )
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrandGreenDark)
        }
    } else if (showCategoryPicker) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (isLoggedIn && !isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showCategoryPicker = false
                            onNewCategoryRequested()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandGreenDark.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrandGreenDark, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("New Category", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = BrandGreenDark)
                }
                Spacer(Modifier.height(2.dp))
            }
            categories.forEach { cat ->
                if (cat.id != categoryId) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onCategorySelect(cat.id, cat.name)
                                showCategoryPicker = false
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            getFundingCategoryIcon(cat.icon),
                            contentDescription = null,
                            tint = LavenderDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(cat.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun FundingNoteField(note: String, onNoteChange: (String) -> Unit) {
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
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CandyBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.EditNote,
                    contentDescription = "Note",
                    tint = LavenderDark,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = TextDark),
                decorationBox = { inner ->
                    if (note.isEmpty()) {
                        Text("Note (optional)...", fontSize = 14.sp, color = TextLight)
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun FundingSubmitButton(isSubmitting: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MintDark),
        enabled = !isSubmitting
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextDark)
        } else {
            Text("Deposit Funds", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
        }
    }
}

private fun getFundingCategoryIcon(iconName: String?): ImageVector {
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
private fun CreateCategoryDialog(
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
        title = { Text("Create New Category", fontWeight = FontWeight.Bold) },
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
                                imageVector = getFundingCategoryIcon(iconName),
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