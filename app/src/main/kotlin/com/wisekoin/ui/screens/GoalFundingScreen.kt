package com.wisekoin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.R
import com.wisekoin.data.model.GoalResponseDto
import com.wisekoin.data.model.WalletResponseDto
import com.wisekoin.ui.components.formatAmount
import com.wisekoin.ui.components.formatVND
import com.wisekoin.ui.theme.BrandGreenDark
import com.wisekoin.ui.theme.CandyBg
import com.wisekoin.ui.theme.MintDark
import com.wisekoin.ui.theme.PrimaryRed
import com.wisekoin.ui.theme.TextDark
import com.wisekoin.ui.theme.TextLight
import com.wisekoin.ui.theme.neomorphicBox
import com.wisekoin.ui.theme.neomorphicInset
import com.wisekoin.viewmodel.GoalFundingError
import com.wisekoin.viewmodel.GoalFundingErrorType
import com.wisekoin.viewmodel.GoalViewModel
import com.wisekoin.viewmodel.WalletViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFundingScreen(
    goalId: Long,
    onClose: () -> Unit,
    goalViewModel: GoalViewModel,
    walletViewModel: WalletViewModel
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedWalletId by rememberSaveable { mutableStateOf<Long?>(null) }
    var localError by remember { mutableStateOf<GoalFundingError?>(null) }

    val goalState by goalViewModel.uiState.collectAsState()
    val walletState by walletViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val goal = goalState.goals.firstOrNull { it.id == goalId }
    val selectedWallet = walletState.wallets.firstOrNull { it.id == selectedWalletId }

    LaunchedEffect(goalId) {
        if (goalViewModel.uiState.value.goals.none { it.id == goalId }) {
            goalViewModel.loadGoals()
        }
        walletViewModel.loadWallets()
    }

    LaunchedEffect(walletState.wallets) {
        if (selectedWalletId == null || walletState.wallets.none { it.id == selectedWalletId }) {
            selectedWalletId = walletState.wallets.maxByOrNull { it.balance }?.id
        }
    }

    val parsedAmount = amount.replace(",", "").toBigDecimalOrNull()
    LaunchedEffect(parsedAmount, walletState.wallets) {
        if (parsedAmount != null && parsedAmount > BigDecimal.ZERO &&
            (selectedWallet == null || selectedWallet.balance < parsedAmount)
        ) {
            selectedWalletId = walletState.wallets
                .filter { it.balance >= parsedAmount }
                .maxByOrNull { it.balance }
                ?.id ?: selectedWalletId
        }
    }

    val dialogError = localError ?: goalState.fundingError
    if (dialogError != null) {
        FundingErrorDialog(
            error = dialogError,
            onDismiss = {
                localError = null
                goalViewModel.clearError()
            }
        )
    }

    Scaffold(
        containerColor = CandyBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.goal_funding_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
            selectedWallet = selectedWallet,
            wallets = walletState.wallets,
            onWalletSelect = { selectedWalletId = it },
            note = note,
            onNoteChange = { note = it },
            isSubmitting = goalState.isFunding,
            onSubmit = {
                when {
                    goal == null -> localError = GoalFundingError(
                        GoalFundingErrorType.GENERAL,
                        context.getString(R.string.goal_not_found_message)
                    )
                    parsedAmount == null || parsedAmount <= BigDecimal.ZERO -> localError = GoalFundingError(
                        GoalFundingErrorType.GENERAL,
                        context.getString(R.string.invalid_contribution_amount)
                    )
                    parsedAmount > goal.remainingAmount -> localError = GoalFundingError(
                        GoalFundingErrorType.CONTRIBUTION_LIMIT,
                        context.getString(R.string.contribution_limit_message, goal.remainingAmount.formatVND())
                    )
                    selectedWallet == null -> localError = GoalFundingError(
                        GoalFundingErrorType.GENERAL,
                        context.getString(R.string.select_wallet_message)
                    )
                    selectedWallet.balance < parsedAmount -> localError = GoalFundingError(
                        GoalFundingErrorType.INSUFFICIENT_BALANCE,
                        context.getString(
                            R.string.insufficient_balance_message,
                            selectedWallet.name,
                            selectedWallet.balance.formatVND(),
                            parsedAmount.formatVND()
                        )
                    )
                    else -> goalViewModel.fundGoal(
                        goalId = goal.id,
                        walletId = selectedWallet.id,
                        amount = parsedAmount,
                        note = note.trim().ifEmpty {
                            context.getString(R.string.goal_funding_default_note, goal.name)
                        },
                        onSuccess = {
                            walletViewModel.loadWallets()
                            Toast.makeText(context, R.string.contribution_success, Toast.LENGTH_SHORT).show()
                            onClose()
                        }
                    )
                }
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
    selectedWallet: WalletResponseDto?,
    wallets: List<WalletResponseDto>,
    onWalletSelect: (Long) -> Unit,
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
        if (goal == null) FundingLoadingBanner() else FundingGoalBanner(goal)
        FundingAmountField(
            amount = amount,
            remainingAmount = goal?.remainingAmount,
            onAmountChange = onAmountChange
        )
        FundingWalletField(
            selectedWallet = selectedWallet,
            wallets = wallets,
            onWalletSelect = onWalletSelect
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
        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryRed)
        }
    }
}

@Composable
private fun FundingGoalBanner(goal: GoalResponseDto) {
    Card(
        modifier = Modifier.fillMaxWidth().neomorphicBox(cornerRadius = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                goal.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${goal.currentAmount.formatVND()} / ${goal.targetAmount.formatVND()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { goal.progressPercent.toFloat().div(100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.goal_progress_percent, goal.progressPercent.toInt()),
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
    remainingAmount: BigDecimal?,
    onAmountChange: (String) -> Unit
) {
    Column {
        Text(
            stringResource(R.string.contribution_amount_label),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextLight
        )
        if (remainingAmount != null && remainingAmount > BigDecimal.ZERO) {
            TextButton(
                onClick = { onAmountChange(formatAmount(remainingAmount.toPlainString())) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    stringResource(R.string.use_remaining_amount, remainingAmount.formatVND()),
                    fontSize = 12.sp,
                    color = BrandGreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neomorphicInset(cornerRadius = 24.dp, elevation = 4.dp)
                .padding(vertical = 14.dp, horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₫", color = PrimaryRed, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(10.dp))
                BasicTextField(
                    value = amount,
                    onValueChange = { onAmountChange(formatAmount(it)) },
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
    selectedWallet: WalletResponseDto?,
    wallets: List<WalletResponseDto>,
    onWalletSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    SelectorItemRow(
        icon = Icons.Outlined.AccountBalanceWallet,
        iconBgColor = MintDark.copy(alpha = 0.35f),
        iconColor = BrandGreenDark,
        title = stringResource(R.string.wallet),
        subtitle = selectedWallet?.let { "${it.name} · ${it.balance.formatVND()}" }
            ?: stringResource(R.string.no_wallet_available),
        trailingIcon = Icons.Outlined.KeyboardArrowDown,
        onClick = { if (wallets.isNotEmpty()) expanded = !expanded }
    )
    if (expanded) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            wallets.forEach { wallet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onWalletSelect(wallet.id)
                            expanded = false
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.AccountBalanceWallet, null, tint = BrandGreenDark)
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(wallet.name, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Text(wallet.type, fontSize = 12.sp, color = TextLight)
                    }
                    Text(
                        wallet.balance.formatVND(),
                        modifier = Modifier.widthIn(max = 132.dp),
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun FundingNoteField(note: String, onNoteChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicInset(cornerRadius = 22.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(CandyBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.EditNote, stringResource(R.string.note), tint = PrimaryRed)
        }
        Spacer(Modifier.size(14.dp))
        BasicTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = TextDark),
            decorationBox = { inner ->
                if (note.isEmpty()) Text(stringResource(R.string.note_optional), color = TextLight)
                inner()
            }
        )
    }
}

@Composable
private fun FundingSubmitButton(isSubmitting: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MintDark),
        enabled = !isSubmitting
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(Modifier.size(20.dp), color = TextDark)
        } else {
            Text(stringResource(R.string.contribute_to_goal), fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

@Composable
private fun FundingErrorDialog(error: GoalFundingError, onDismiss: () -> Unit) {
    val insufficient = error.type == GoalFundingErrorType.INSUFFICIENT_BALANCE
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(PrimaryRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.AccountBalanceWallet, null, tint = PrimaryRed)
            }
        },
        title = {
            Text(
                stringResource(
                    if (insufficient) R.string.insufficient_balance_title
                    else R.string.contribution_problem_title
                ),
                fontWeight = FontWeight.Bold
            )
        },
        text = { Text(error.message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)) {
                Text(
                    stringResource(
                        if (insufficient) R.string.choose_another_wallet else R.string.adjust_amount
                    )
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dismiss)) } }
    )
}
