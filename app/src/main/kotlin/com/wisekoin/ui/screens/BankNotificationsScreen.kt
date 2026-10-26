package com.wisekoin.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.R
import com.wisekoin.data.model.BankNotificationCandidate
import com.wisekoin.data.model.BankNotificationDirection
import com.wisekoin.notification.BankNotificationAccess
import com.wisekoin.notification.BankNotificationAlertManager
import com.wisekoin.ui.theme.CandyBg
import com.wisekoin.ui.theme.PrimaryRed
import com.wisekoin.util.MoneyFormatter
import com.wisekoin.viewmodel.BankNotificationReviewError
import com.wisekoin.viewmodel.BankNotificationUiState
import com.wisekoin.viewmodel.BankNotificationViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankNotificationsScreen(
    viewModel: BankNotificationViewModel,
    baseCurrency: String,
    initialCandidateId: String? = null,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var accessGranted by remember { mutableStateOf(BankNotificationAccess.isGranted(context)) }
    var alertAccessGranted by remember {
        mutableStateOf(BankNotificationAlertManager.canNotify(context))
    }
    var enableAfterSettings by rememberSaveable { mutableStateOf(false) }
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    var settingsUnavailable by rememberSaveable { mutableStateOf(false) }
    var alertPermissionRequested by rememberSaveable { mutableStateOf(false) }
    var consumedCandidateId by rememberSaveable { mutableStateOf<String?>(null) }
    val alertPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> alertAccessGranted = granted }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        accessGranted = BankNotificationAccess.isGranted(context)
        alertAccessGranted = BankNotificationAlertManager.canNotify(context)
        if (accessGranted && enableAfterSettings) {
            viewModel.setTrackingEnabled(true)
            enableAfterSettings = false
        } else if (!accessGranted && state.trackingEnabled) {
            viewModel.setTrackingEnabled(false)
        }
    }

    LaunchedEffect(initialCandidateId, state.candidates) {
        if (initialCandidateId != null &&
            consumedCandidateId != initialCandidateId &&
            state.candidates.any { it.id == initialCandidateId }
        ) {
            consumedCandidateId = initialCandidateId
            viewModel.openCandidate(initialCandidateId, baseCurrency)
        }
    }

    LaunchedEffect(state.saveSuccessVersion) {
        if (state.saveSuccessVersion > 0) {
            snackbarHostState.showSnackbar(context.getString(R.string.bank_notification_saved))
        }
    }

    Scaffold(
        containerColor = CandyBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bank_notification_tracking)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TrackingCard(
                    accessGranted = accessGranted,
                    alertAccessGranted = alertAccessGranted,
                    trackingEnabled = state.trackingEnabled,
                    settingsUnavailable = settingsUnavailable,
                    onGrantAccess = {
                        enableAfterSettings = true
                        settingsUnavailable = false
                        try {
                            context.startActivity(BankNotificationAccess.settingsIntent())
                        } catch (_: ActivityNotFoundException) {
                            enableAfterSettings = false
                            settingsUnavailable = true
                        }
                    },
                    onTrackingChanged = viewModel::setTrackingEnabled,
                    onGrantAlertAccess = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !alertPermissionRequested
                        ) {
                            alertPermissionRequested = true
                            alertPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        } else {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                )
                            } catch (_: ActivityNotFoundException) {
                                settingsUnavailable = true
                            }
                        }
                    }
                )
            }

            if (state.isSyncing) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(stringResource(R.string.syncing_bank_suggestions))
                    }
                }
            } else if (
                state.reviewError == BankNotificationReviewError.SUGGESTION_SYNC_FAILED &&
                state.selectedCandidate == null
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                state.networkErrorMessage
                                    ?: stringResource(R.string.bank_suggestion_sync_failed),
                                color = MaterialTheme.colorScheme.error
                            )
                            OutlinedButton(
                                onClick = viewModel::syncPending,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.bank_notification_review_queue),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.bank_notification_queue_privacy),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.candidates.isNotEmpty()) {
                        TextButton(onClick = { showClearConfirmation = true }) {
                            Text(stringResource(R.string.clear_all))
                        }
                    }
                }
            }

            if (state.candidates.isEmpty()) {
                item { EmptyQueueCard() }
            } else {
                items(state.candidates, key = BankNotificationCandidate::id) { candidate ->
                    CandidateCard(
                        candidate = candidate,
                        onReview = { viewModel.openCandidate(candidate.id, baseCurrency) },
                        onDiscard = { viewModel.discard(candidate.id) }
                    )
                }
            }

            item { Spacer(Modifier.padding(bottom = 8.dp)) }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.clear_bank_notifications_title)) },
            text = { Text(stringResource(R.string.clear_bank_notifications_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAll()
                        showClearConfirmation = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed
                    )
                ) {
                    Text(stringResource(R.string.clear_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    state.selectedCandidate?.let {
        CandidateReviewDialog(
            state = state,
            onDismiss = viewModel::closeReview,
            onAmountChanged = viewModel::updateAmount,
            onNoteChanged = viewModel::updateNote,
            onWalletSelected = viewModel::selectWallet,
            onCategorySelected = viewModel::selectCategory,
            onRetry = viewModel::retryOptions,
            onSave = viewModel::saveSelected
        )
    }
}

@Composable
private fun TrackingCard(
    accessGranted: Boolean,
    alertAccessGranted: Boolean,
    trackingEnabled: Boolean,
    settingsUnavailable: Boolean,
    onGrantAccess: () -> Unit,
    onTrackingChanged: (Boolean) -> Unit,
    onGrantAlertAccess: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.bank_notification_tracking),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (accessGranted) {
                            stringResource(R.string.notification_access_granted)
                        } else {
                            stringResource(R.string.notification_access_required)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = trackingEnabled && accessGranted,
                    onCheckedChange = onTrackingChanged,
                    enabled = accessGranted
                )
            }
            Text(
                stringResource(R.string.bank_notification_privacy_explanation),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(R.string.bank_notification_pilot_sources),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!accessGranted) {
                Button(
                    onClick = onGrantAccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.grant_notification_access))
                }
            }
            if (accessGranted && !alertAccessGranted) {
                Text(
                    stringResource(R.string.bank_notification_alert_access_detail),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = onGrantAlertAccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.allow_review_alerts))
                }
                Text(
                    stringResource(R.string.review_alerts_denied),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (settingsUnavailable) {
                Text(
                    stringResource(R.string.notification_settings_unavailable),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EmptyQueueCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
            Text(
                stringResource(R.string.no_bank_notifications),
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.no_bank_notifications_detail),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CandidateCard(
    candidate: BankNotificationCandidate,
    onReview: () -> Unit,
    onDiscard: () -> Unit
) {
    val directionLabel = if (candidate.direction == BankNotificationDirection.EXPENSE) {
        stringResource(R.string.expense)
    } else {
        stringResource(R.string.income)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        candidate.sourceApp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(
                            R.string.bank_notification_direction_time,
                            directionLabel,
                            formatCandidateTime(candidate.occurredAt)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    MoneyFormatter.format(candidate.amount, candidate.currency),
                    fontWeight = FontWeight.Bold,
                    color = if (candidate.direction == BankNotificationDirection.EXPENSE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(
                    onClick = onDiscard,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                    Text(stringResource(R.string.discard))
                }
                Button(
                    onClick = onReview,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.review))
                }
            }
        }
    }
}

@Composable
private fun CandidateReviewDialog(
    state: BankNotificationUiState,
    onDismiss: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onWalletSelected: (Long) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit
) {
    val candidate = requireNotNull(state.selectedCandidate)
    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        title = { Text(stringResource(R.string.review_bank_transaction)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.bank_notification_review_explanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.amount_with_currency, candidate.currency)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = !state.isSubmitting,
                    isError = state.reviewError == BankNotificationReviewError.INVALID_AMOUNT
                )
                if (state.isLoadingOptions) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                } else {
                    ReviewSelector(
                        label = stringResource(R.string.wallet),
                        selectedLabel = state.wallets.firstOrNull {
                            it.id == state.selectedWalletId
                        }?.name,
                        emptyLabel = stringResource(R.string.select_wallet),
                        options = state.wallets.map { it.id to it.name },
                        enabled = !state.isSubmitting,
                        onSelected = onWalletSelected
                    )
                    ReviewSelector(
                        label = stringResource(R.string.category),
                        selectedLabel = state.categories.firstOrNull {
                            it.id == state.selectedCategoryId
                        }?.name,
                        emptyLabel = stringResource(R.string.select_category),
                        options = state.categories.map { it.id to it.name },
                        enabled = !state.isSubmitting,
                        onSelected = onCategorySelected
                    )
                }
                OutlinedTextField(
                    value = state.note,
                    onValueChange = onNoteChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.note_optional)) },
                    enabled = !state.isSubmitting,
                    maxLines = 3
                )
                reviewErrorMessage(state)?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (state.reviewError == BankNotificationReviewError.OPTIONS_LOAD_FAILED) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !state.isLoadingOptions &&
                    !state.isSyncing &&
                    !state.isSubmitting &&
                    candidate.suggestionId != null &&
                    state.reviewError != BankNotificationReviewError.CURRENCY_MISMATCH &&
                    state.reviewError != BankNotificationReviewError.OPTIONS_LOAD_FAILED
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.save_transaction))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isSubmitting) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ReviewSelector(
    label: String,
    selectedLabel: String?,
    emptyLabel: String,
    options: List<Pair<Long, String>>,
    enabled: Boolean,
    onSelected: (Long) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled && options.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    selectedLabel ?: emptyLabel,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onSelected(id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun reviewErrorMessage(state: BankNotificationUiState): String? = when (state.reviewError) {
    BankNotificationReviewError.INVALID_AMOUNT -> stringResource(R.string.amount_invalid)
    BankNotificationReviewError.SELECT_WALLET -> stringResource(R.string.select_wallet_message)
    BankNotificationReviewError.SELECT_CATEGORY -> stringResource(R.string.select_category)
    BankNotificationReviewError.CURRENCY_MISMATCH -> stringResource(
        R.string.bank_notification_currency_mismatch,
        state.selectedCandidate?.currency.orEmpty(),
        state.baseCurrency
    )
    BankNotificationReviewError.OPTIONS_LOAD_FAILED,
    BankNotificationReviewError.SUGGESTION_SYNC_FAILED,
    BankNotificationReviewError.SAVE_FAILED -> state.networkErrorMessage
        ?: stringResource(R.string.bank_notification_save_failed)
    null -> null
}

private fun formatCandidateTime(value: String): String = runCatching {
    LocalDateTime.parse(value).format(
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    )
}.getOrDefault(value)
