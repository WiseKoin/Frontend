package com.wisekoin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.GoalViewModel
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
fun CreateGoalScreen(
    onClose: () -> Unit,
    onGoalCreated: () -> Unit,
    goalViewModel: GoalViewModel
) {
    val context = LocalContext.current
    val uiState by goalViewModel.uiState.collectAsState()

    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var deadlineMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

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
        CreateGoalContent(
            padding = padding,
            goalName = goalName,
            onGoalNameChange = { goalName = it },
            targetAmount = targetAmount,
            onTargetAmountChange = { targetAmount = it },
            deadlineText = deadlineMillis.formatDeadline(),
            onDeadlineClick = { showDatePicker = true },
            isSubmitting = uiState.isCreating,
            onSubmit = {
                if (goalName.isBlank()) {
                    Toast.makeText(context, "Please enter a goal title.", Toast.LENGTH_SHORT).show()
                    return@CreateGoalContent
                }
                val parsedAmount = targetAmount.replace(",", "").toBigDecimalOrNull()
                if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
                    Toast.makeText(context, "Please enter a valid target amount.", Toast.LENGTH_SHORT).show()
                    return@CreateGoalContent
                }
                goalViewModel.createGoal(
                    name = goalName,
                    targetAmount = parsedAmount,
                    deadline = deadlineMillis?.let { LocalDate.ofEpochDay(it / 86400000).toString() },
                    onSuccess = {
                        onGoalCreated()
                    },
                    onError = { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    deadlineMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
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
}

@Composable
private fun CreateGoalContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    goalName: String,
    onGoalNameChange: (String) -> Unit,
    targetAmount: String,
    onTargetAmountChange: (String) -> Unit,
    deadlineText: String,
    onDeadlineClick: () -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = goalName,
            onValueChange = onGoalNameChange,
            label = { Text("Goal Title") },
            placeholder = { Text("e.g. New Laptop") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = targetAmount,
            onValueChange = onTargetAmountChange,
            label = { Text("Target Amount (VND)") },
            placeholder = { Text("e.g. 20000000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neomorphicBox(cornerRadius = 22.dp)
                .background(CandyBg, RoundedCornerShape(22.dp))
                .clickable { onDeadlineClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Deadline", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Medium)
                    Text(deadlineText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Icon(Icons.Default.DateRange, contentDescription = "Pick date", tint = TextDark)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text("Create Goal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

private fun Long?.formatDeadline(): String {
    if (this == null) return "No deadline (optional)"
    return try {
        LocalDate.ofEpochDay(this / 86400000)
            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } catch (e: Exception) {
        "No deadline (optional)"
    }
}
