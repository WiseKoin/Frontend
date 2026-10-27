package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.R
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.data.model.DebtRequest
import com.wisekoin.data.model.DebtResponse
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.ui.components.formatAmount
import com.wisekoin.ui.components.formatCurrency
import com.wisekoin.ui.components.LocalCurrencyCode
import com.wisekoin.ui.components.NetworkErrorBanner
import com.wisekoin.ui.theme.*
import com.wisekoin.util.MoneyFormatter
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.DebtViewModel
import com.wisekoin.viewmodel.WalletViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeableDebtItem(
    debt: DebtResponse,
    isLent: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPaid: () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value -> value != SwipeToDismissBoxValue.EndToStart }
    )
    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryRed)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_debt),
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        DebtCardItem(debt, isLent, onEdit, onPaid)
    }
}

@Composable
fun DebtCardItem(
    debt: DebtResponse,
    isLent: Boolean,
    onEdit: () -> Unit,
    onPaid: () -> Unit
) {
    val currencyCode = LocalCurrencyCode.current

    val dueDateText = remember(debt.dueDate) {
        debt.dueDate?.let { rawDate ->
            runCatching {
                LocalDate.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }.getOrElse { rawDate }
        } ?: "No due date"
    }

    val isCompleted = debt.status == "COMPLETED"
    val isOverdue = debt.status == "OVERDUE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 18.dp)
            .clickable(role = Role.Button, onClick = onEdit),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLent) MintDark.copy(alpha = 0.2f)
                        else PeachDark.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLent) {
                        Icons.Default.ArrowUpward
                    } else {
                        Icons.Default.ArrowDownward
                    },
                    contentDescription = null,
                    tint = if (isLent) MintDark else PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = debt.counterpartName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = dueDateText,
                    fontSize = 12.sp,
                    color = if (isOverdue) PrimaryRed else TextLight,
                    maxLines = 1
                )

                debt.note?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = TextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = debt.remainingAmount.formatCurrency(currencyCode),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1
                )

                if (isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintDark.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TextDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stringResource(R.string.settled),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onPaid,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(9.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintDark
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.paid),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun EditDebtDialog(
    debt: DebtResponse,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (DebtRequest) -> Unit
) {
    val currencyCode = LocalCurrencyCode.current
    var counterpartName by rememberSaveable(debt.id) { mutableStateOf(debt.counterpartName) }
    var type by rememberSaveable(debt.id) { mutableStateOf(debt.type) }
    var amount by rememberSaveable(debt.id, currencyCode) {
        mutableStateOf(
            MoneyFormatter.format(
                debt.totalAmount,
                currencyCode,
                includeCurrencyCode = false
            )
        )
    }
    var dueDate by rememberSaveable(debt.id) { mutableStateOf(debt.dueDate.orEmpty()) }
    var note by rememberSaveable(debt.id) { mutableStateOf(debt.note.orEmpty()) }
    var validationMessage by rememberSaveable(debt.id) { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(stringResource(R.string.edit_debt), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = counterpartName,
                    onValueChange = { counterpartName = it; validationMessage = null },
                    label = { Text(stringResource(R.string.counterpart_name)) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LEND", "BORROW").forEach { option ->
                        OutlinedButton(
                            onClick = { type = option },
                            enabled = !isSubmitting,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (type == option) MintDark.copy(alpha = 0.25f) else Color.Transparent
                            )
                        ) {
                            Text(
                                stringResource(
                                    if (option == "LEND") R.string.debt_type_lend
                                    else R.string.debt_type_borrow
                                )
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = formatAmount(it, currencyCode)
                        validationMessage = null
                    },
                    label = { Text(stringResource(R.string.total_amount_vnd, currencyCode)) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (MoneyFormatter.currencyScale(currencyCode) == 0) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Decimal
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it.take(10); validationMessage = null },
                    label = { Text(stringResource(R.string.due_date_optional)) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_optional)) },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                validationMessage?.let {
                    Text(stringResource(it), color = MaterialTheme.colorScheme.error)
                }
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = MoneyFormatter.parse(amount, currencyCode)
                    val parsedDate = dueDate.takeIf(String::isNotBlank)?.let {
                        runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
                    }
                    validationMessage = when {
                        counterpartName.isBlank() -> R.string.debt_name_required
                        parsedAmount == null || parsedAmount <= BigDecimal.ZERO ||
                            parsedAmount < debt.paidAmount -> R.string.invalid_debt_amount
                        dueDate.isNotBlank() && parsedDate == null -> R.string.invalid_due_date
                        else -> null
                    }
                    if (validationMessage == null) {
                        onSave(
                            DebtRequest(
                                counterpartName = counterpartName.trim(),
                                type = type,
                                totalAmount = parsedAmount!!,
                                dueDate = parsedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                note = note.trim().ifBlank { null }
                            )
                        )
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.save_changes))
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
internal fun RepayDebtDialog(
    debt: DebtResponse,
    wallets: List<WalletResponseDto>,
    categories: List<CategoryResponse>,
    optionsLoading: Boolean,
    isSubmitting: Boolean,
    submissionFailed: Boolean,
    onDismiss: () -> Unit,
    onRepay: (BigDecimal, Long, Long) -> Unit
) {
    val currencyCode = LocalCurrencyCode.current
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
                    stringResource(
                        R.string.remaining_debt,
                        debt.remainingAmount.formatCurrency(currencyCode)
                    ),
                    color = TextLight,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = formatAmount(it, currencyCode)
                        validationMessage = null
                    },
                    label = { Text(stringResource(R.string.amount_vnd, currencyCode)) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (MoneyFormatter.currencyScale(currencyCode) == 0) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Decimal
                        }
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
                    options = wallets.map {
                        it.id to "${it.name} · ${it.balance.formatCurrency(currencyCode)}"
                    },
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
                    val parsedAmount = MoneyFormatter.parse(amount, currencyCode)
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
internal fun RepaymentSelector(
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

