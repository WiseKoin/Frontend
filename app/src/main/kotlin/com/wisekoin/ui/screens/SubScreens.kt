package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRecordingScreen(onClose: () -> Unit) {
    Scaffold(
        containerColor = CandyBg,
        topBar = {
            TopAppBar(
                title = { Text("Voice Input AI", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .neomorphicBox(cornerRadius = 60.dp)
                    .clip(CircleShape)
                    .background(PrimaryRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(54.dp))
            }
            Spacer(Modifier.height(32.dp))
            Text("Listening...", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(8.dp))
            Text("Speak e.g.: \"Spent 50,000 VND on lunch today\"", fontSize = 14.sp, color = TextLight)
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onClose,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Stop & Process", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalScreen(onClose: () -> Unit) {
    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }

    Scaffold(
        containerColor = CandyBg,
        topBar = {
            TopAppBar(
                title = { Text("Create Savings Goal", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = { Text("Goal Title") },
                placeholder = { Text("e.g. New Laptop") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text("Target Amount (VND)") },
                placeholder = { Text("e.g. 20000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Create Goal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFundsScreen(onClose: () -> Unit) {
    var amount by remember { mutableStateOf("") }

    Scaffold(
        containerColor = CandyBg,
        topBar = {
            TopAppBar(
                title = { Text("Add Funds to Goal", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Deposit Amount (VND)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintDark)
            ) {
                Text("Deposit Funds", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            }
        }
    }
}
