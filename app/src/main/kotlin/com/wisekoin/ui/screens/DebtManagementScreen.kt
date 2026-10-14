package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    debtViewModel: DebtViewModel,
    categoryViewModel: CategoryViewModel,
    walletViewModel: WalletViewModel,
    isLoggedIn: Boolean = false,
    onAddDebt: () -> Unit
) {
    val state by debtViewModel.uiState.collectAsStateWithLifecycle()
    val categoryState by categoryViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
    var isLentTab by remember { mutableStateOf(true) }
    var stateToRepay by remember { mutableStateOf<Pair<DebtResponse, Boolean>?>(null) }
    var debtToEdit by remember { mutableStateOf<DebtResponse?>(null) }
    var debtToDelete by remember { mutableStateOf<DebtResponse?>(null) }
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
        state.mutationError?.let { message ->
            item {
                NetworkErrorBanner(message = message, onRetry = debtViewModel::clearError)
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

            items(currentList, key = { it.id }) { debt: DebtResponse ->
                SwipeableDebtItem(
                    debt = debt,
                    isLent = isLentTab,
                    onEdit = { debtToEdit = debt },
                    onDelete = { debtToDelete = debt },
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

    debtToEdit?.let { debt ->
        EditDebtDialog(
            debt = debt,
            isSubmitting = state.editingDebtId == debt.id,
            errorMessage = state.mutationError,
            onDismiss = {
                debtViewModel.clearError()
                debtToEdit = null
            },
            onSave = { request ->
                scope.launch {
                    if (debtViewModel.updateDebt(debt.id, request)) debtToEdit = null
                }
            }
        )
    }

    debtToDelete?.let { debt ->
        AlertDialog(
            onDismissRequest = {
                if (state.deletingDebtId == null) {
                    debtViewModel.clearError()
                    debtToDelete = null
                }
            },
            title = { Text(stringResource(R.string.delete_debt)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.delete_debt_confirmation, debt.counterpartName))
                    state.mutationError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (debtViewModel.deleteDebt(debt.id)) debtToDelete = null
                        }
                    },
                    enabled = state.deletingDebtId == null,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    if (state.deletingDebtId == debt.id) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.delete))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { debtToDelete = null },
                    enabled = state.deletingDebtId == null
                ) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
