package com.wisekoin.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wisekoin.R
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.ui.components.AppTopBar
import com.wisekoin.ui.components.NetworkErrorBanner
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.ReceiptViewModel
import com.wisekoin.viewmodel.WalletViewModel

@Composable
internal fun ReceiptImageSection(
    imageUri: Uri?,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .neomorphicBox(cornerRadius = 28.dp, elevation = 5.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CandyBg)
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Receipt Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1554415707-9e4426c62c76?auto=format&fit=crop&w=600&q=80",
                    contentDescription = "Placeholder Receipt",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .neomorphicBox(cornerRadius = 20.dp, elevation = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable { onRetake() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retake",
                    tint = BrandGreenDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Retake",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreenDark
                )
            }
        }
    }
}

/**
 * Thẻ tên cửa hàng (Store Name) với Inline Editing
 */
@Composable
internal fun StoreNameCard(
    storeName: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp, elevation = 5.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STORE NAME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Store Name",
                    tint = BrandGreenDark,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .neomorphicInset(cornerRadius = 16.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = storeName,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    ),
                    cursorBrush = SolidColor(BrandGreenDark),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Hàng 2 cột Date và Category
 */
@Composable
internal fun DateAndCategoryRow(
    dateText: String,
    currentCategoryId: Long?,
    onCategoryChange: (Long?, String) -> Unit,
    categoryViewModel: CategoryViewModel,
    isLoggedIn: Boolean
) {
    val categoryUiState by categoryViewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToManage by remember { mutableStateOf<CategoryResponse?>(null) }
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cột 1: Date Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(115.dp)
                .neomorphicBox(cornerRadius = 24.dp, elevation = 5.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CandyBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = TextLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = dateText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    lineHeight = 18.sp
                )
            }
        }

        // Cột 2: Category Card
        Card(
            modifier = Modifier
                .weight(1.1f)
                .neomorphicBox(cornerRadius = 24.dp, elevation = 5.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CandyBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header: CATEGORY label + New Category button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        letterSpacing = 0.5.sp
                    )
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
                            modifier = Modifier.size(14.dp),
                            tint = if (isLoggedIn) BrandGreenDark else TextLight
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "New Category",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoggedIn) BrandGreenDark else TextLight
                        )
                    }
                }

                // Category chips grid
                if (categoryUiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = BrandGreenDark,
                            strokeWidth = 2.dp
                        )
                    }
                } else if (!isLoggedIn) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Empty list (Guest mode)",
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }
                } else if (categoryUiState.categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Empty list. Create a new category!",
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }
                } else {
                    val categories = categoryUiState.categories
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowItems.forEach { cat ->
                                    CategoryChip(
                                        name = cat.name,
                                        icon = getIconForCategory(cat.icon),
                                        isSelected = cat.id == currentCategoryId,
                                        onClick = { onCategoryChange(cat.id, cat.name) },
                                        onLongClick = { categoryToManage = cat }
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateCategoryDialog(
            type = "Expense",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, icon ->
                categoryViewModel.createCategory(
                    name = name,
                    type = "Expense",
                    icon = icon,
                    isLoggedIn = isLoggedIn,
                    onSuccess = {},
                    onError = {}
                )
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
                categoryViewModel.updateCategory(category, name, icon) {
                    if (currentCategoryId == category.id) {
                        onCategoryChange(category.id, name)
                    }
                    categoryToManage = null
                }
            },
            onDelete = {
                categoryViewModel.deleteCategory(category.id) {
                    if (currentCategoryId == category.id) onCategoryChange(null, "")
                    categoryToManage = null
                }
            }
        )
    }
}

/**
 * Thẻ số tiền (Total Amount) với Inline Editing
 */
@Composable
internal fun TotalAmountCard(
    amount: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp, elevation = 5.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL AMOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Amount",
                    tint = BrandGreenDark,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .neomorphicInset(cornerRadius = 18.dp)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = amount,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandGreenDark
                        ),
                        cursorBrush = SolidColor(BrandGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = "VND",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
            }
        }
    }
}

// --------------------------------------------------
// Wallet Selector (same style as SelectorItemRow in AddTransactionScreen)
// --------------------------------------------------
@Composable
internal fun WalletSelector(
    selectedWalletId: Long?,
    walletOptions: List<Pair<Long, String>>,
    onWalletSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = walletOptions.firstOrNull { it.first == selectedWalletId }?.second ?: "Select wallet"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (walletOptions.size > 1) expanded = !expanded }
            .neomorphicBox(cornerRadius = 22.dp, elevation = 5.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MintDark.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AccountBalanceWallet,
                        null,
                        tint = BrandGreenDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Wallet", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
                    Text(selectedName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
            Icon(
                imageVector = if (walletOptions.size > 1) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = TextDark,
                modifier = Modifier.size(22.dp)
            )
        }
    }

    if (expanded && walletOptions.size > 1) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            walletOptions.forEach { (id, name) ->
                if (id != selectedWalletId) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .clickable {
                                onWalletSelect(id)
                                expanded = false
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
}

// --------------------------------------------------
// Compact category chip for the receipt screen
// --------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CategoryChip(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) BrandGreenDark else CandyBg.copy(alpha = 0.5f))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) Color.White else TextDark,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextDark
            )
        }
    }
}

// --------------------------------------------------
// Dialog tạo danh mục mới (same pattern as AddTransactionScreen)
// --------------------------------------------------
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

