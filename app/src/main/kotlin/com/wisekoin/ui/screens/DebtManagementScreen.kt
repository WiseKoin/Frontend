package com.wisekoin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.ui.theme.*

data class DebtItem(
    val id: String,
    val personName: String,
    val note: String,
    val amount: String,
    val dueDate: String,
    val isOverdue: Boolean,
    val isPaid: Boolean
)

val sampleLentDebts = listOf(
    DebtItem("1", "David Smith", "Lunch & Movie", "550,000đ", "15 Nov 2025", false, false),
    DebtItem("2", "Emma Watson", "Concert Ticket", "1,200,000đ", "01 Nov 2025", true, false)
)

val sampleBorrowedDebts = listOf(
    DebtItem("3", "John Doe", "Shared Airbnb", "800,000đ", "20 Nov 2025", false, false)
)

@Composable
fun DebtManagementScreen(
    onAddDebt: () -> Unit
) {
    var isLentTab by remember { mutableStateOf(true) }

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

        // Lent / Borrowed Segmented Toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CandyBg, RoundedCornerShape(50))
                    .neomorphic(cornerRadius = 50.dp)
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

        // Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neomorphic(cornerRadius = 24.dp),
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
                            if (isLentTab) "1,750,000đ" else "800,000đ",
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

        // Debt Items Header
        item {
            Text(
                if (isLentTab) "People Who Owe You" else "People You Owe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        val currentList = if (isLentTab) sampleLentDebts else sampleBorrowedDebts

        items(currentList) { item ->
            DebtCardItem(debt = item, isLent = isLentTab)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DebtCardItem(debt: DebtItem, isLent: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphic(cornerRadius = 20.dp),
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
                    Text(debt.personName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("${debt.note} • Due: ${debt.dueDate}", fontSize = 12.sp, color = TextLight)
                    if (debt.isOverdue) {
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
                Text(debt.amount, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintDark)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextDark)
                    Spacer(Modifier.width(4.dp))
                    Text("Paid", fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
