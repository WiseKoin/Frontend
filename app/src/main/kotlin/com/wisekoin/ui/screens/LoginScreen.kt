package com.wisekoin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.R
import com.wisekoin.ui.theme.WiseKoinTheme
import com.wisekoin.ui.theme.neomorphicCardShadow
import com.wisekoin.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = WiseKoinTheme.colors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Neomorphic Piggy Bank Circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(WiseKoinTheme.colors.Surface)
                    .neomorphicCardShadow(shape = CircleShape, elevation = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = stringResource(R.string.piggy_bank),
                    tint = WiseKoinTheme.colors.Primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Titles
            Text(
                text = stringResource(R.string.welcome_back_title),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = WiseKoinTheme.colors.OnBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = WiseKoinTheme.colors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Form Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neomorphicCardShadow(shape = RoundedCornerShape(32.dp), elevation = 10.dp)
                    .border(3.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WiseKoinTheme.colors.Surface.copy(alpha = 0.92f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Email Field
                    Text(
                        text = stringResource(R.string.email_address),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = WiseKoinTheme.colors.OnSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        placeholder = { Text(stringResource(R.string.email_placeholder), color = WiseKoinTheme.colors.OnSurfaceVariant.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Mail, contentDescription = null, tint = WiseKoinTheme.colors.Primary)
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = WiseKoinTheme.colors.Primary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Password Field
                    Text(
                        text = stringResource(R.string.password),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = WiseKoinTheme.colors.OnSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        placeholder = { Text(stringResource(R.string.password_placeholder), color = WiseKoinTheme.colors.OnSurfaceVariant.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = WiseKoinTheme.colors.Primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = stringResource(
                                        if (passwordVisible) R.string.hide_password else R.string.show_password
                                    ),
                                    tint = WiseKoinTheme.colors.OnSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = WiseKoinTheme.colors.Primary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.login(
                                onSuccess = onLoginSuccess,
                                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                            )
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Forgot Password
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, end = 4.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = stringResource(R.string.forgot_password_question),
                            color = WiseKoinTheme.colors.Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clickable(role = Role.Button) { onForgotPasswordClick() }
                        )
                    }

                    if (uiState.loginErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.loginErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Let's Go! Button
                    Button(
                        onClick = {
                            viewModel.login(
                                onSuccess = onLoginSuccess,
                                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WiseKoinTheme.colors.Primary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.login_action),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Sign Up text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.new_to_wisekoin) + " ",
                    fontSize = 16.sp,
                    color = WiseKoinTheme.colors.OnBackground,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.sign_up),
                    fontSize = 18.sp,
                    color = WiseKoinTheme.colors.Primary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(role = Role.Button) { onNavigateToSignUp() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
