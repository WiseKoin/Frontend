package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onClose: () -> Unit,
    onVoiceAdd: () -> Unit,
    onScanReceipt: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Dining") }
    var isExpense by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = CandyBg,
        topBar = {
            TopAppBar(
                title = { Text("New Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CandyBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Quick AI Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onVoiceAdd,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Voice Input", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onScanReceipt,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Scan Receipt", fontWeight = FontWeight.Bold)
                }
            }

            // Type Segmented Switch (Expense / Income)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CandyBg, RoundedCornerShape(50))
                    .neomorphicBox(cornerRadius = 50.dp)
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { isExpense = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isExpense) PrimaryRed else Color.Transparent,
                        contentColor = if (isExpense) Color.White else TextLight
                    )
                ) {
                    Text("Expense", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { isExpense = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isExpense) MintDark else Color.Transparent,
                        contentColor = if (!isExpense) TextDark else TextLight
                    )
                ) {
                    Text("Income", fontWeight = FontWeight.Bold)
                }
            }

            // Amount Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neomorphicBox(cornerRadius = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CandyBg)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Amount (VND)", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            // Note / Title Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neomorphicBox(cornerRadius = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CandyBg)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Transaction Note", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Lunch with colleagues") },
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Save Transaction", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
