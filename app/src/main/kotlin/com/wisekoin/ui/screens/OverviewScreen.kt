package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.R
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.ui.components.*
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.WalletViewModel
import java.math.BigDecimal

@Composable
fun OverviewScreen(
    userName: String = "Guest",
    isLoggedIn: Boolean = false,
    walletViewModel: WalletViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val state by walletViewModel.uiState.collectAsStateWithLifecycle()
    var showCreateWalletDialog by rememberSaveable { mutableStateOf(false) }
    var selectedWallet by remember { mutableStateOf<WalletResponseDto?>(null) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            walletViewModel.refreshAll()
        }
    }

    val displayBalance = if (isLoggedIn)
        state.totalBalance.formatVND()
    else "0 VND"

    val displayIncome = if (isLoggedIn)
        state.currentMonthIncome.formatVND()
    else "0 VND"

    val displayExpense = if (isLoggedIn)
        state.currentMonthExpense.formatVND()
    else "0 VND"

    val displayTransactions = if (isLoggedIn) {
        state.recentTransactions.map { tx ->
            val categoryLabel = tx.category?.name
                ?: tx.goal?.let { "Savings goal" }
                ?: "Transfer"
            val isExpense = tx.type != "INCOME"
            Transaction(
                id = tx.id.toString(),
                title = tx.note ?: tx.goal?.name ?: categoryLabel,
                category = categoryLabel,
                amount = if (isExpense) {
                    "-${tx.amount.toLong().formatStringVND()}"
                } else {
                    "+${tx.amount.toLong().formatStringVND()}"
                },
                isExpense = isExpense,
                time = formatTransactionTime(tx.transactionDate),
                iconBgColor = if (isExpense) PeachDark else MintDark
            )
        }
    } else emptyList()

    Scaffold(
        containerColor = CandyBg,
        topBar = {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                AppTopBar(
                    onNotificationClick = {},
                    onProfileClick = onProfileClick,
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .background(color = CandyBg)
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                WalletOverviewSection(
                    wallets = if (isLoggedIn) state.wallets else emptyList(),
                    isLoading = isLoggedIn && state.isLoading,
                    canAddWallet = isLoggedIn,
                    onAddWallet = { showCreateWalletDialog = true },
                    onWalletClick = { selectedWallet = it }
                )
            }

            state.error?.let { message ->
                item {
                    NetworkErrorBanner(message = message, onRetry = walletViewModel::refreshAll)
                }
            }

            item {
                TotalBalanceCard(
                    balance = displayBalance,
                    income = displayIncome,
                    expense = displayExpense
                )
            }

            item {
                Button(
                    onClick = onNavigateToAddTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add Transaction",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            item {
                CashFlowCard(stats = if (isLoggedIn) state.monthlyStats else emptyList())
            }

            item {
                RecentTransactionsSection(
                    transactions = displayTransactions,
                    onSeeAll = onSeeAllTransactions
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showCreateWalletDialog) {
        OverviewCreateWalletDialog(
            isSubmitting = state.isCreatingWallet,
            submissionFailed = state.walletCreationFailed,
            onDismiss = {
                walletViewModel.clearWalletCreationError()
                showCreateWalletDialog = false
            },
            onCreate = { name, type, balance ->
                walletViewModel.createWallet(name, type, balance) {
                    showCreateWalletDialog = false
                }
            }
        )
    }

    selectedWallet?.let { wallet ->
        WalletDetailsDialog(
            wallet = wallet,
            isDeleting = state.deletingWalletId == wallet.id,
            errorMessage = state.walletMutationError,
            onDismiss = {
                walletViewModel.clearWalletMutationError()
                selectedWallet = null
            },
            onDelete = {
                walletViewModel.deleteWallet(wallet.id) { selectedWallet = null }
            }
        )
    }
}

@Composable
private fun WalletOverviewSection(
    wallets: List<WalletResponseDto>,
    isLoading: Boolean,
    canAddWallet: Boolean,
    onAddWallet: () -> Unit,
    onWalletClick: (WalletResponseDto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.wallets_title),
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onAddWallet,
                enabled = canAddWallet,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintDark,
                    contentColor = TextDark
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.add_wallet), fontWeight = FontWeight.Bold)
            }
        }
        when {
            isLoading && wallets.isEmpty() -> Box(
                modifier = Modifier.fillMaxWidth().height(92.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandGreenDark)
            }
            wallets.isEmpty() -> Text(
                text = stringResource(R.string.wallets_empty),
                fontSize = 13.sp,
                color = TextLight
            )
            else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wallets, key = { it.id }) { wallet ->
                    Card(
                        modifier = Modifier.width(190.dp).clickable { onWalletClick(wallet) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CandyBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = BrandGreenDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    wallet.name,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                wallet.balance.formatVND(),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(wallet.type, fontSize = 11.sp, color = TextLight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletDetailsDialog(
    wallet: WalletResponseDto,
    isDeleting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by rememberSaveable(wallet.id) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(stringResource(R.string.wallet_details), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(wallet.name, style = MaterialTheme.typography.titleLarge, color = TextDark)
                Text(wallet.balance.formatVND(), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(walletTypeLabel(wallet.type)), color = TextLight)
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (confirmDelete) {
                    Text(
                        stringResource(R.string.delete_wallet_confirmation, wallet.name),
                        color = PrimaryRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (confirmDelete) onDelete() else confirmDelete = true
                },
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.delete_wallet))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun OverviewCreateWalletDialog(
    isSubmitting: Boolean,
    submissionFailed: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, BigDecimal) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf("CASH") }
    var initialBalance by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<Int?>(null) }
    val walletTypes = listOf("CASH", "BANK", "E_WALLET", "CREDIT_CARD")

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(stringResource(R.string.create_wallet_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text(stringResource(R.string.wallet_name)) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    isError = validationError == R.string.wallet_name_required,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.wallet_type),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextLight
                )
                walletTypes.chunked(2).forEach { rowTypes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTypes.forEach { type ->
                            val selected = selectedType == type
                            Button(
                                onClick = { selectedType = type },
                                enabled = !isSubmitting,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MintDark else CandyBg,
                                    contentColor = if (selected) Color.White else TextDark
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text(
                                    stringResource(walletTypeLabel(type)),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = initialBalance,
                    onValueChange = {
                        initialBalance = formatAmount(it)
                        validationError = null
                    },
                    label = { Text(stringResource(R.string.initial_balance_vnd)) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    isError = validationError == R.string.invalid_initial_balance,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                validationError?.let {
                    Text(stringResource(it), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (submissionFailed) {
                    Text(
                        stringResource(R.string.wallet_creation_failed),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = initialBalance.replace(",", "").toBigDecimalOrNull()
                        ?: BigDecimal.ZERO.takeIf { initialBalance.isBlank() }
                    when {
                        name.isBlank() -> validationError = R.string.wallet_name_required
                        balance == null || balance < BigDecimal.ZERO ->
                            validationError = R.string.invalid_initial_balance
                        else -> onCreate(name.trim(), selectedType, balance)
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.create))
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

private fun walletTypeLabel(type: String): Int = when (type) {
    "BANK" -> R.string.wallet_type_bank
    "E_WALLET" -> R.string.wallet_type_e_wallet
    "CREDIT_CARD" -> R.string.wallet_type_credit_card
    else -> R.string.wallet_type_cash
}
