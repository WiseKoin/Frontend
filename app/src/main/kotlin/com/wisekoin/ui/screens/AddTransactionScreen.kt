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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.CategoryViewModel
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
fun AddTransactionScreen(
    onClose: () -> Unit,
    onVoiceAdd: () -> Unit,
    onScanReceipt: () -> Unit,
    onConfirmSuccess: () -> Unit,
    onDebtCreated: () -> Unit,
    onProfileClick: () -> Unit,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
    transactionEntryViewModel: TransactionEntryViewModel
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

    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn = authUiState.isLoggedIn
    val categoryUiState by categoryViewModel.uiState.collectAsStateWithLifecycle()
    val transactionEntryState by transactionEntryViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedType, isLoggedIn) {
        if (selectedType in listOf("Expense", "Income", "Lend", "Borrow")) {
            categoryViewModel.loadCategories(selectedType, isLoggedIn)
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) transactionEntryViewModel.loadWallets()
    }

    LaunchedEffect(transactionEntryState.wallets) {
        walletOptions = transactionEntryState.wallets.map { it.id to it.name }
        if (selectedWalletId == null) {
            transactionEntryState.wallets.firstOrNull()?.let { wallet ->
                selectedWalletId = wallet.id
                selectedWalletName = wallet.name
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
            onUpdateCategory = { categoryToUpdate, name, icon, onSuccess ->
                categoryViewModel.updateCategory(categoryToUpdate, name, icon, onSuccess)
            },
            onDeleteCategory = { categoryToDelete, onSuccess ->
                categoryViewModel.deleteCategory(categoryToDelete.id, onSuccess)
            },
            counterpartName = counterpartName,
            onCounterpartNameChange = { counterpartName = it },
            debtDueDateMillis = debtDueDateMillis,
            onDebtDueDateChange = { debtDueDateMillis = it },
            showDebtDueDatePicker = showDebtDueDatePicker,
            onDebtDueDatePickerOpen = { showDebtDueDatePicker = true }
        )
        categoryUiState.errorMessage?.let { error ->
            NetworkErrorBanner(
                message = error,
                onRetry = { categoryViewModel.loadCategories(selectedType, isLoggedIn) }
            )
        }
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
        transactionEntryState.walletLoadError?.let { error ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(error, color = PrimaryRed, modifier = Modifier.weight(1f))
                TextButton(onClick = transactionEntryViewModel::loadWallets) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
        if (showCreateWalletDialog && isLoggedIn) {
            CreateWalletDialog(
                transactionEntryViewModel = transactionEntryViewModel,
                onDismiss = { showCreateWalletDialog = false },
                onWalletCreated = { newWalletId, newWalletName ->
                    showCreateWalletDialog = false
                    selectedWalletId = newWalletId
                    selectedWalletName = newWalletName
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
                        transactionEntryViewModel.createDebt(
                            request = DebtRequest(
                                counterpartName = counterpartName.trim(),
                                type = debtType,
                                totalAmount = parsedAmount,
                                dueDate = dueDate,
                                note = if (note.isBlank()) null else note
                            ),
                            onSuccess = onDebtCreated,
                            onFailure = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                } else {
                    val parsedAmount = amount.replace(",", "").toBigDecimalOrNull()
                    if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
                        Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                    } else {
                        val backendType = if (selectedType == "Income") "INCOME" else "EXPENSE"
                        transactionEntryViewModel.createTransaction(
                            request = TransactionRequest(
                                walletId = selectedWalletId!!,
                                categoryId = selectedCategoryId!!,
                                amount = parsedAmount,
                                type = backendType,
                                note = if (note.isBlank()) null else note,
                                transactionDate = formatTransactionDate(selectedDateMillis, selectedHour, selectedMinute)
                            ),
                            onSuccess = onConfirmSuccess,
                            onFailure = { message ->
                                val safeMessage = if (message.contains("Insufficient", ignoreCase = true)) {
                                    "Insufficient wallet balance."
                                } else {
                                    message
                                }
                                Toast.makeText(context, safeMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            },
            isSubmitting = transactionEntryState.isSubmitting
        )
        Spacer(Modifier.height(8.dp))
    }
}

// -------------------------------------------------------------
// 2. Cụm điều khiển loại giao dịch & Số tiền
// -------------------------------------------------------------
