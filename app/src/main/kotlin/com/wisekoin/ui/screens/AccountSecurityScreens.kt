package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.R
import com.wisekoin.data.model.OtpPurpose
import com.wisekoin.ui.theme.CandyBg
import com.wisekoin.ui.theme.PrimaryRed
import com.wisekoin.ui.theme.TextDark
import com.wisekoin.ui.theme.TextLight
import com.wisekoin.viewmodel.AccountSecurityUiState
import com.wisekoin.viewmodel.AccountSecurityViewModel

@Composable
fun AccountSecurityScreen(
    email: String,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    SecurityScaffold(title = stringResource(R.string.account_security), onBack = onBack) {
        Text(
            text = stringResource(R.string.account_activity),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = TextDark)
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.current_session), fontWeight = FontWeight.Bold)
                    Text(email, color = TextLight, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        stringResource(R.string.security_action_notice),
                        color = TextLight,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Button(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) {
            Icon(Icons.Default.LockReset, contentDescription = null)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text(stringResource(R.string.change_password))
        }

        OutlinedButton(
            onClick = onDeleteAccount,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text(stringResource(R.string.delete_account))
        }
    }
}

@Composable
fun ChangePasswordScreen(
    viewModel: AccountSecurityViewModel,
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    val currentPasswordRequired = stringResource(R.string.current_password_required)
    val passwordStrengthError = stringResource(R.string.password_strength_error)
    val passwordMismatchError = stringResource(R.string.passwords_do_not_match)

    LaunchedEffect(Unit) { viewModel.begin(OtpPurpose.CHANGE_PASSWORD) }

    SecurityScaffold(title = stringResource(R.string.change_password), onBack = onBack) {
        Text(
            stringResource(R.string.password_requirements),
            color = TextLight,
            style = MaterialTheme.typography.bodyMedium
        )
        PasswordField(
            value = currentPassword,
            onValueChange = { currentPassword = it; validationError = null },
            label = stringResource(R.string.current_password),
            enabled = !state.isSubmitting
        )
        PasswordField(
            value = newPassword,
            onValueChange = { newPassword = it; validationError = null },
            label = stringResource(R.string.new_password),
            enabled = !state.isSubmitting
        )
        PasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; validationError = null },
            label = stringResource(R.string.confirm_new_password),
            enabled = !state.isSubmitting
        )
        OtpVerificationSection(
            state = state,
            code = code,
            onCodeChange = { code = it.filter(Char::isDigit).take(6); viewModel.clearError() },
            onRequestCode = viewModel::requestCode,
            onVerifyCode = { viewModel.verifyCode(code) }
        )
        validationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                validationError = when {
                    currentPassword.isBlank() -> currentPasswordRequired
                    !isStrongPassword(newPassword) -> passwordStrengthError
                    newPassword != confirmPassword -> passwordMismatchError
                    else -> null
                }
                if (validationError == null) {
                    viewModel.changePassword(currentPassword, newPassword, onPasswordChanged)
                }
            },
            enabled = state.verified && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.change_password))
            }
        }
    }
}

@Composable
fun DeleteAccountScreen(
    viewModel: AccountSecurityViewModel,
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var showFinalConfirmation by rememberSaveable { mutableStateOf(false) }
    val currentPasswordRequired = stringResource(R.string.current_password_required)
    val deleteConfirmationError = stringResource(R.string.delete_confirmation_error)

    LaunchedEffect(Unit) { viewModel.begin(OtpPurpose.DELETE_ACCOUNT) }

    SecurityScaffold(title = stringResource(R.string.delete_account), onBack = onBack) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PrimaryRed.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                stringResource(R.string.delete_account_warning),
                modifier = Modifier.padding(16.dp),
                color = PrimaryRed,
                fontWeight = FontWeight.SemiBold
            )
        }
        PasswordField(
            value = currentPassword,
            onValueChange = { currentPassword = it; validationError = null },
            label = stringResource(R.string.current_password),
            enabled = !state.isSubmitting
        )
        OutlinedTextField(
            value = confirmation,
            onValueChange = { confirmation = it; validationError = null },
            label = { Text(stringResource(R.string.type_delete_to_confirm)) },
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        OtpVerificationSection(
            state = state,
            code = code,
            onCodeChange = { code = it.filter(Char::isDigit).take(6); viewModel.clearError() },
            onRequestCode = viewModel::requestCode,
            onVerifyCode = { viewModel.verifyCode(code) }
        )
        validationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                validationError = when {
                    currentPassword.isBlank() -> currentPasswordRequired
                    confirmation != "DELETE" -> deleteConfirmationError
                    else -> null
                }
                if (validationError == null) showFinalConfirmation = true
            },
            enabled = state.verified && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
        ) {
            Text(stringResource(R.string.delete_account_permanently))
        }
    }

    if (showFinalConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!state.isSubmitting) showFinalConfirmation = false },
            title = { Text(stringResource(R.string.final_confirmation)) },
            text = { Text(stringResource(R.string.delete_account_final_warning)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAccount(currentPassword, onAccountDeleted) },
                    enabled = !state.isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.delete))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFinalConfirmation = false },
                    enabled = !state.isSubmitting
                ) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun OtpVerificationSection(
    state: AccountSecurityUiState,
    code: String,
    onCodeChange: (String) -> Unit,
    onRequestCode: () -> Unit,
    onVerifyCode: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.email_verification), fontWeight = FontWeight.Bold)
        state.destination?.let {
            Text(stringResource(R.string.code_sent_to, it), color = TextLight)
        }
        OutlinedButton(
            onClick = onRequestCode,
            enabled = !state.isRequestingCode && state.resendSecondsRemaining == 0L,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            if (state.isRequestingCode) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    if (state.codeRequested && state.resendSecondsRemaining > 0) {
                        stringResource(R.string.resend_code_in, state.resendSecondsRemaining)
                    } else if (state.codeRequested) {
                        stringResource(R.string.resend_code)
                    } else {
                        stringResource(R.string.send_verification_code)
                    }
                )
            }
        }
        if (state.codeRequested) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    label = { Text(stringResource(R.string.verification_code)) },
                    singleLine = true,
                    enabled = !state.verified && !state.isVerifyingCode,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onVerifyCode,
                    enabled = code.length == 6 && !state.verified && !state.isVerifyingCode,
                    modifier = Modifier.heightIn(min = 56.dp)
                ) {
                    Text(
                        if (state.verified) stringResource(R.string.verified)
                        else stringResource(R.string.verify)
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SecurityScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = CandyBg,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

private fun isStrongPassword(password: String): Boolean =
    password.length in 8..128 &&
        password.any(Char::isUpperCase) &&
        password.any(Char::isLowerCase) &&
        password.any(Char::isDigit) &&
        password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
