package com.wisekoin.ui.screens

import com.wisekoin.ui.components.NetworkErrorBanner

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.GoalViewModel
import com.wisekoin.viewmodel.VoiceViewModel
import com.wisekoin.viewmodel.WalletViewModel
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRecordingScreen(
    onClose: () -> Unit,
    onConfirmSuccess: () -> Unit = {},
    voiceViewModel: VoiceViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
    walletViewModel: WalletViewModel
) {
    val context = LocalContext.current
    val uiState by voiceViewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val walletUiState by walletViewModel.uiState.collectAsStateWithLifecycle()
    val categoryUiState by categoryViewModel.uiState.collectAsStateWithLifecycle()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            voiceViewModel.startRecording(context)
        } else {
            Toast.makeText(context, "Microphone access is required for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

    var selectedWalletId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {
            walletViewModel.loadWallets()
        }
    }

    LaunchedEffect(walletUiState.wallets) {
        if (selectedWalletId == null && walletUiState.wallets.isNotEmpty()) {
            selectedWalletId = walletUiState.wallets[0].id
        }
    }

    LaunchedEffect(uiState.editableType, authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn &&
            (uiState.editableType == "EXPENSE" || uiState.editableType == "INCOME")
        ) {
            categoryViewModel.loadCategories(uiState.editableType, true)
        }
    }

    Scaffold(
        containerColor = CandyBg,
        topBar = {
            TopAppBar(
                title = { Text("Voice Input AI", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = {
                        voiceViewModel.resetState()
                        onClose()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (uiState.voiceResult != null) Arrangement.Top else Arrangement.Center
        ) {
            walletUiState.error?.let { error ->
                NetworkErrorBanner(message = error, onRetry = walletViewModel::loadWallets)
                Spacer(Modifier.height(12.dp))
            }
            categoryUiState.errorMessage?.let { error ->
                NetworkErrorBanner(
                    message = error,
                    onRetry = {
                        categoryViewModel.loadCategories(uiState.editableType, authUiState.isLoggedIn)
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
            if (uiState.voiceResult != null) {
                // ==========================================
                // RESULT STATE: AI Extracted Transaction
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neomorphicBox(cornerRadius = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CandyBg)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Extracted Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryRed)
                        }

                        if (uiState.transcript.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(LemonDark.copy(alpha = 0.15f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "“${uiState.transcript}”",
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = TextDark
                                )
                            }
                        }

                        // Type Toggle (Expense / Income)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = uiState.editableType.equals("EXPENSE", ignoreCase = true),
                                onClick = { voiceViewModel.updateType("EXPENSE") },
                                label = { Text("Expense", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = CandyBg,
                                    labelColor = TextLight
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            FilterChip(
                                selected = uiState.editableType.equals("INCOME", ignoreCase = true),
                                onClick = { voiceViewModel.updateType("INCOME") },
                                label = { Text("Income", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MintDark,
                                    selectedLabelColor = TextDark,
                                    containerColor = CandyBg,
                                    labelColor = TextLight
                                ),
                                shape = RoundedCornerShape(50)
                            )
                        }

                        // Amount
                        OutlinedTextField(
                            value = uiState.editableAmount,
                            onValueChange = { voiceViewModel.updateAmount(it) },
                            label = { Text("Amount (${uiState.currency})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Note / Merchant
                        OutlinedTextField(
                            value = uiState.editableNote,
                            onValueChange = { voiceViewModel.updateNote(it) },
                            label = { Text("Note / Description") },
                            placeholder = { Text("e.g. Ăn trưa, Cafe") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Category Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            val categories = if (
                                uiState.editableType == "EXPENSE" || uiState.editableType == "INCOME"
                            ) {
                                categoryUiState.categories
                            } else {
                                emptyList()
                            }
                            if (categories.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(categories) { cat ->
                                        val isSelected = cat.id == uiState.editableCategoryId
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { voiceViewModel.updateCategory(cat.id, cat.name) },
                                            label = { Text(cat.name, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PeachDark,
                                                selectedLabelColor = Color.White,
                                                containerColor = CandyBg,
                                                labelColor = TextDark
                                            ),
                                            shape = RoundedCornerShape(50)
                                        )
                                    }
                                }
                            } else {
                                Text("Select a transaction type to load categories.", fontSize = 13.sp, color = TextDark)
                            }
                        }

                        // Wallet Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Wallet", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            val wallets = walletUiState.wallets
                            if (wallets.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(wallets) { wallet ->
                                        val isSelected = wallet.id == selectedWalletId
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedWalletId = wallet.id },
                                            label = { Text(wallet.name, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = LemonDark,
                                                selectedLabelColor = TextDark,
                                                containerColor = CandyBg,
                                                labelColor = TextDark
                                            ),
                                            shape = RoundedCornerShape(50)
                                        )
                                    }
                                }
                            } else {
                                Text("No wallet available. Please create a wallet first.", fontSize = 13.sp, color = PrimaryRed)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Action Buttons
                        Button(
                            onClick = {
                                voiceViewModel.confirmAndSave(
                                    categoryViewModel = categoryViewModel,
                                    selectedWalletId = selectedWalletId,
                                    isLoggedIn = authUiState.isLoggedIn,
                                    onSuccess = {
                                        Toast.makeText(context, "Transaction saved successfully!", Toast.LENGTH_SHORT).show()
                                        onConfirmSuccess()
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                            enabled = !uiState.isSubmitting
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Text("Confirm & Save", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { voiceViewModel.resetState() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Record Again", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // ==========================================
                // RECORDING / IDLE / PROCESSING STATE
                // ==========================================
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = if (uiState.isRecording) 1.25f else 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .neomorphicBox(cornerRadius = 65.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isRecording) PrimaryRed else CandyBg)
                        .clickable(enabled = !uiState.isProcessing) {
                            if (uiState.isRecording) {
                                voiceViewModel.stopAndProcess()
                            } else {
                                if (hasAudioPermission) {
                                    voiceViewModel.startRecording(context)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (uiState.isRecording) Color.White else PrimaryRed,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                if (uiState.isProcessing) {
                    CircularProgressIndicator(color = PrimaryRed, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Gemini AI is analyzing your speech...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Extracting amount, category & note...", fontSize = 13.sp, color = TextLight)
                } else if (uiState.isRecording) {
                    val minutes = uiState.recordingSeconds / 60
                    val seconds = uiState.recordingSeconds % 60
                    Text(
                        String.format("%02d:%02d", minutes, seconds),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryRed
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Listening... Tap icon to finish", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = { voiceViewModel.stopAndProcess() },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop & Process", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Tap to Speak", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Say e.g.: \"Ăn bún bò 45 nghìn\"\nor \"Coffee with friends 35k\"",
                        fontSize = 14.sp,
                        color = TextLight,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = {
                            if (hasAudioPermission) {
                                voiceViewModel.startRecording(context)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Recording", fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = PrimaryRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
    val uiState by goalViewModel.uiState.collectAsStateWithLifecycle()

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
