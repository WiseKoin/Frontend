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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisekoin.data.model.DebtRepaymentRequest
import com.wisekoin.data.model.DebtResponse
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.ui.components.formatVND
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.DebtViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.widget.Toast

@Composable
fun DebtManagementScreen(
    debtViewModel: DebtViewModel = viewModel(),
    onAddDebt: () -> Unit
) {
    val state by debtViewModel.uiState.collectAsState()
    var isLentTab by remember { mutableStateOf(true) }
    var stateToRepay by remember { mutableStateOf<Pair<DebtResponse, Boolean>?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    .padding(4.dp),
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
        showRepayDialog(
            debt = debt,
            isLent = isLent,
            onRepaid = {
                stateToRepay = null
                scope.launch { debtViewModel.loadDebts() }
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
private fun showRepayDialog(
    debt: DebtResponse,
    isLent: Boolean,
    onRepaid: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var walletName by remember { mutableStateOf("Loading...") }
    var walletId by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.walletApi.getWallets()
            if (response.wallets.isNotEmpty()) {
                val first = response.wallets[0]
                walletId = first.id
                walletName = first.name
            }
        } catch (e: Exception) {
            walletName = "Main Cash"
        }
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Repay Debt", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (VND)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neomorphicBox(cornerRadius = 22.dp)
                        .background(CandyBg, RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text("Wallet", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
                        Text(walletName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.replace(",", "").toBigDecimalOrNull()
                    if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
                        Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (walletId == null) {
                        Toast.makeText(context, "Please select a wallet.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            RetrofitClient.debtApi.repayDebt(
                                debt.id,
                                DebtRepaymentRequest(
                                    amount = parsedAmount,
                                    walletId = walletId!!,
                                    categoryId = if (isLent) 1L else 2L,
                                    note = null
                                )
                            )
                            onRepaid()
                        } catch (e: Exception) {
                            Toast.makeText(context, e.localizedMessage ?: "Failed to repay debt.", Toast.LENGTH_SHORT).show()
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
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {}) { Text("Cancel") }
        }
    )
}
