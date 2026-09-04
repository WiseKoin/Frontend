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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisekoin.ui.components.AppTopBar
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.ReceiptViewModel

// -------------------------------------------------------------
// 1. Màn hình chính (Scaffold & State Management)
// -------------------------------------------------------------
@Composable
fun AddTransactionScreen(
    onClose: () -> Unit,
    onVoiceAdd: () -> Unit,
    onScanReceipt: () -> Unit,
    onConfirmSuccess: () -> Unit,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
    receiptViewModel: ReceiptViewModel
) {
    var selectedType by remember { mutableStateOf("Expense") }
    var amount by remember { mutableStateOf("1,250,000") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val authUiState by authViewModel.uiState.collectAsState()
    val isLoggedIn = authUiState.isLoggedIn
    val categoryUiState by categoryViewModel.uiState.collectAsState()

    LaunchedEffect(selectedType, isLoggedIn) {
        if (selectedType in listOf("Expense", "Income")) {
            categoryViewModel.loadCategories(selectedType, isLoggedIn)
        }
    }

    Column(
        modifier = Modifier
            .background(color = CandyBg)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTopBar()
        AmountSection(selectedType, { selectedType = it }, amount, { amount = it }, onClose)
        MiddleDynamicSection(
            type = selectedType,
            selectedCategory = category,
            onCategorySelect = { category = it },
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
            }
        )
        CommonTransactionFields(note) { note = it }
        QuickToolActions(onVoiceAdd, onScanReceipt)
        SaveTransactionButton(
            onClick = {
                receiptViewModel.confirmAndSave(onSuccess = onConfirmSuccess)
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
                onValueChange = onAmountChange,
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
    onCategorySelect: (String) -> Unit,
    isLoggedIn: Boolean,
    categoryUiState: com.wisekoin.viewmodel.CategoryUiState,
    onCreateCategory: (String, String) -> Unit
) {
    if (type in listOf("Lend", "Borrow")) {
        LendBorrowFields()
    } else {
        CategoryCard(
            type = type,
            selectedCategory = selectedCategory,
            onCategorySelect = onCategorySelect,
            isLoggedIn = isLoggedIn,
            categoryUiState = categoryUiState,
            onCreateCategory = onCreateCategory
        )
    }
}

@Composable
private fun LendBorrowFields() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SelectorItemRow(
            icon = Icons.Outlined.PersonAddAlt,
            iconBgColor = PrimaryRed.copy(alpha = 0.12f),
            iconColor = PrimaryRed,
            title = "Lender/Borrower Name",
            subtitle = "Select contact"
        )
        SelectorItemRow(
            icon = Icons.Outlined.CalendarToday,
            iconBgColor = LemonLight,
            iconColor = LemonDark,
            title = "Due Date",
            subtitle = "Set due date",
            trailingIcon = Icons.Outlined.CalendarMonth
        )
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
    onCategorySelect: (String) -> Unit,
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
                                    icon = getIconForCategory(cat.icon),
                                    isSelected = selectedCategory == cat.name,
                                    onClick = { onCategorySelect(cat.name) }
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
private fun CommonTransactionFields(note: String, onNoteChange: (String) -> Unit) {
    SelectorItemRow(
        icon = Icons.Outlined.AccountBalanceWallet,
        iconBgColor = MintDark.copy(alpha = 0.35f),
        iconColor = BrandGreenDark,
        title = "Wallet",
        subtitle = "Main Cash"
    )
    SelectorItemRow(
        icon = Icons.Outlined.CalendarToday,
        iconBgColor = LavenderDark.copy(alpha = 0.2f),
        iconColor = LavenderDark,
        title = "Date & Time",
        subtitle = "Today, 14:30"
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
