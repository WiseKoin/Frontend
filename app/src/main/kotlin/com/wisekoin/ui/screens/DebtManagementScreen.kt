package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.R
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.data.model.DebtResponse
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.ui.components.formatAmount
import com.wisekoin.ui.components.formatVND
import com.wisekoin.ui.components.NetworkErrorBanner
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.DebtViewModel
import com.wisekoin.viewmodel.WalletViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DebtManagementScreen(
    debtViewModel: DebtViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    walletViewModel: WalletViewModel = viewModel(),
    isLoggedIn: Boolean = false,
    onAddDebt: () -> Unit
) {
    val state by debtViewModel.uiState.collectAsStateWithLifecycle()
    val categoryState by categoryViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
    var isLentTab by remember { mutableStateOf(true) }
    var stateToRepay by remember { mutableStateOf<Pair<DebtResponse, Boolean>?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        debtViewModel.loadDebts()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CandyBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text("Debt Management", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextDark)
            Text("Easily track money lent and money borrowed.", fontSize = 14.sp, color = TextLight)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CandyBg, RoundedCornerShape(50))
                    .neomorphicBox(cornerRadius = 50.dp)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { isLentTab = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLentTab) PrimaryRed else Color.Transparent,
                        contentColor = if (isLentTab) Color.White else TextLight
                    )
                ) {
                    Text("Money Lent", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { isLentTab = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isLentTab) PrimaryRed else Color.Transparent,
                        contentColor = if (!isLentTab) Color.White else TextLight
                    )
                ) {
                    Text("Money Borrowed", fontWeight = FontWeight.Bold)
                }
            }
        }

        state.errorMessage?.let { message ->
            item {
                NetworkErrorBanner(message = message, onRetry = { debtViewModel.loadDebts() })
            }
        }

        item {
            val total: BigDecimal = if (isLentTab) state.totalLent else state.totalBorrowed
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neomorphicBox(cornerRadius = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CandyBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isLentTab) "Total Money Lent" else "Total Money Borrowed",
                            fontSize = 13.sp,
                            color = TextLight,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            total.formatVND(),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isLentTab) MintDark else PrimaryRed
                        )
                    }

                    FloatingActionButton(
                        onClick = onAddDebt,
                        containerColor = PrimaryRed,
                        contentColor = Color.White,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Debt Entry")
                    }
                }
            }
        }

        item {
            Text(
                if (isLentTab) "People Who Owe You" else "People You Owe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandGreenDark)
                }
            }
        } else {
            val currentList: List<DebtResponse> = if (isLentTab) state.lentDebts else state.borrowedDebts

            items(currentList) { debt: DebtResponse ->
                DebtCardItem(
                    debt = debt,
                    isLent = isLentTab,
                    onPaid = {
                        stateToRepay = debt to isLentTab
                    }
                )
            }

            if (currentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No ${if (isLentTab) "lent" else "borrowed"} debts yet",
                            fontSize = 14.sp,
                            color = TextLight
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    stateToRepay?.let { (debt, isLent) ->
        LaunchedEffect(debt.id, isLent, isLoggedIn) {
            walletViewModel.loadWallets()
            categoryViewModel.loadCategories(if (isLent) "Lend" else "Borrow", isLoggedIn)
        }
        RepayDebtDialog(
            debt = debt,
            wallets = walletState.wallets,
            categories = categoryState.categories,
            optionsLoading = walletState.isLoading || categoryState.isLoading,
            isSubmitting = state.isRepaying,
            submissionFailed = state.repaymentFailed,
            onDismiss = {
                debtViewModel.clearError()
                stateToRepay = null
            },
            onRepay = { amount, walletId, categoryId ->
                scope.launch {
                    if (debtViewModel.repayDebt(
                            debtId = debt.id,
                            amount = amount,
                            walletId = walletId,
                            categoryId = categoryId
                        )
                    ) {
                        stateToRepay = null
                    }
                }
            }
        )
    }
}

@Composable
fun DebtCardItem(debt: DebtResponse, isLent: Boolean, onPaid: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isLent) MintDark.copy(alpha = 0.2f) else PeachDark.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isLent) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isLent) MintDark else PrimaryRed
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(debt.counterpartName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    val dueDateText = debt.dueDate?.let {
                        try {
                            val date = LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                            date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        } catch (e: Exception) { it }
                    } ?: "No due date"
                    val noteText = debt.note?.takeIf { it.isNotEmpty() } ?: ""
                    Text("${noteText} • Due: $dueDateText", fontSize = 12.sp, color = TextLight)
                    if (debt.status == "OVERDUE") {
                        Surface(
                            color = PrimaryRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                "Overdue",
                                color = PrimaryRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    debt.remainingAmount.formatVND(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(Modifier.height(4.dp))
                if (debt.status != "COMPLETED") {
                    Button(
                        onClick = onPaid,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintDark)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextDark)
                        Spacer(Modifier.width(4.dp))
                        Text("Paid", fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        "Settled",
                        fontSize = 11.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RepayDebtDialog(
    debt: DebtResponse,
    wallets: List<WalletResponseDto>,
    categories: List<CategoryResponse>,
    optionsLoading: Boolean,
    isSubmitting: Boolean,
    submissionFailed: Boolean,
    onDismiss: () -> Unit,
    onRepay: (BigDecimal, Long, Long) -> Unit
) {
    var amount by rememberSaveable(debt.id) { mutableStateOf("") }
    var selectedWalletId by rememberSaveable(debt.id) { mutableStateOf<Long?>(null) }
    var selectedCategoryId by rememberSaveable(debt.id) { mutableStateOf<Long?>(null) }
    var validationMessage by rememberSaveable(debt.id) { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(stringResource(R.string.repay_debt), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.remaining_debt, debt.remainingAmount.formatVND()),
                    color = TextLight,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = formatAmount(it)
                        validationMessage = null
                    },
                    label = { Text(stringResource(R.string.amount_vnd)) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                RepaymentSelector(
                    label = stringResource(R.string.wallet),
                    placeholder = stringResource(
                        if (wallets.isEmpty() && !optionsLoading) {
                            R.string.no_wallet_available
                        } else {
                            R.string.select_wallet
                        }
                    ),
                    selectedId = selectedWalletId,
                    options = wallets.map { it.id to "${it.name} · ${it.balance.formatVND()}" },
                    leadingIcon = {
                        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null)
                    },
                    enabled = !isSubmitting && wallets.isNotEmpty(),
                    onSelected = {
                        selectedWalletId = it
                        validationMessage = null
                    }
                )
                RepaymentSelector(
                    label = stringResource(R.string.category),
                    placeholder = stringResource(
                        if (categories.isEmpty() && !optionsLoading) {
                            R.string.no_compatible_category
                        } else {
                            R.string.select_category
                        }
                    ),
                    selectedId = selectedCategoryId,
                    options = categories.map { it.id to it.name },
                    leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null) },
                    enabled = !isSubmitting && categories.isNotEmpty(),
                    onSelected = {
                        selectedCategoryId = it
                        validationMessage = null
                    }
                )
                if (optionsLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                validationMessage?.let {
                    Text(
                        stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                if (submissionFailed) {
                    Text(
                        stringResource(R.string.debt_repayment_failed),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.replace(",", "").toBigDecimalOrNull()
                    when {
                        parsedAmount == null || parsedAmount <= BigDecimal.ZERO ->
                            validationMessage = R.string.invalid_repayment_amount
                        parsedAmount > debt.remainingAmount ->
                            validationMessage = R.string.repayment_exceeds_remaining
                        selectedWalletId == null -> validationMessage = R.string.select_wallet
                        selectedCategoryId == null -> validationMessage = R.string.select_category
                        else -> onRepay(parsedAmount, selectedWalletId!!, selectedCategoryId!!)
                    }
                },
                enabled = !isSubmitting && !optionsLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenDark)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun RepaymentSelector(
    label: String,
    placeholder: String,
    selectedId: Long?,
    options: List<Pair<Long, String>>,
    leadingIcon: @Composable () -> Unit,
    enabled: Boolean,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.firstOrNull { it.first == selectedId }?.second ?: placeholder

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            leadingIcon()
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, fontSize = 11.sp, color = TextLight)
                Text(
                    selectedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextDark
                )
            }
            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
