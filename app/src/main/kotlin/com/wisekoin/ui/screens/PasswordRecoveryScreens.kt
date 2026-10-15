package com.wisekoin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.R
import com.wisekoin.viewmodel.PasswordRecoveryViewModel

@Composable
fun PasswordRecoveryRequestScreen(
    viewModel: PasswordRecoveryViewModel,
    onBack: () -> Unit,
    onCodeSent: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember(state.email) { mutableStateOf(state.email) }
    var validationError by remember { mutableStateOf<Int?>(null) }
    RecoveryScaffold(R.string.forgot_password, onBack) {
        Text(stringResource(R.string.recovery_email_instruction))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; validationError = null; viewModel.clearError() },
            label = { Text(stringResource(R.string.email_address)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        RecoveryError(validationError?.let { stringResource(it) } ?: state.errorMessage)
        Button(
            onClick = {
                validationError = if (email.isBlank() || !email.contains('@')) {
                    R.string.invalid_email
                } else {
                    null
                }
                if (validationError == null) viewModel.requestCode(email, onCodeSent)
            },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            SubmissionLabel(state.isSubmitting, R.string.send_verification_code)
        }
    }
}

@Composable
fun PasswordRecoveryOtpScreen(
    viewModel: PasswordRecoveryViewModel,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<Int?>(null) }
    RecoveryScaffold(R.string.email_verification, onBack) {
        Text(stringResource(R.string.recovery_code_instruction, state.email))
        OutlinedTextField(
            value = code,
            onValueChange = {
                code = it.filter(Char::isDigit).take(6)
                validationError = null
                viewModel.clearError()
            },
            label = { Text(stringResource(R.string.verification_code)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        RecoveryError(validationError?.let { stringResource(it) } ?: state.errorMessage)
        Button(
            onClick = {
                validationError = if (code.length != 6) R.string.invalid_verification_code else null
                if (validationError == null) viewModel.verifyCode(code, onVerified)
            },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            SubmissionLabel(state.isSubmitting, R.string.verify)
        }
        TextButton(
            onClick = viewModel::resendCode,
            enabled = !state.isSubmitting && state.resendSeconds == 0L
        ) {
            Text(
                if (state.resendSeconds > 0) {
                    stringResource(R.string.resend_code_in, state.resendSeconds)
                } else {
                    stringResource(R.string.resend_code)
                }
            )
        }
    }
}

@Composable
fun PasswordRecoveryNewPasswordScreen(
    viewModel: PasswordRecoveryViewModel,
    onBack: () -> Unit,
    onPasswordReset: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<Int?>(null) }
    RecoveryScaffold(R.string.create_new_password, onBack) {
        Text(stringResource(R.string.password_requirements))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; validationError = null; viewModel.clearError() },
            label = { Text(stringResource(R.string.new_password)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = confirmation,
            onValueChange = { confirmation = it; validationError = null; viewModel.clearError() },
            label = { Text(stringResource(R.string.confirm_new_password)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        RecoveryError(validationError?.let { stringResource(it) } ?: state.errorMessage)
        Button(
            onClick = {
                validationError = when {
                    !password.isStrongPassword() -> R.string.password_strength_error
                    password != confirmation -> R.string.passwords_do_not_match
                    else -> null
                }
                if (validationError == null) {
                    viewModel.resetPassword(password) { onPasswordReset(state.email) }
                }
            },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            SubmissionLabel(state.isSubmitting, R.string.reset_password)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryScaffold(
    title: Int,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun RecoveryError(message: String?) {
    if (message != null) Text(message, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun SubmissionLabel(isSubmitting: Boolean, label: Int) {
    if (isSubmitting) {
        CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
    } else {
        Text(stringResource(label))
    }
}

private fun String.isStrongPassword(): Boolean =
    length in 8..128 &&
        any(Char::isUpperCase) &&
        any(Char::isLowerCase) &&
        any(Char::isDigit) &&
        any { !it.isLetterOrDigit() && !it.isWhitespace() }
