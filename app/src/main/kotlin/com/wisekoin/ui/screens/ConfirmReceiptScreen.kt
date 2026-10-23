package com.wisekoin.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wisekoin.R
import com.wisekoin.ui.components.AppTopBar
import com.wisekoin.ui.components.NetworkErrorBanner
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.ReceiptViewModel
import com.wisekoin.viewmodel.WalletViewModel

@Composable
fun ConfirmReceiptScreen(
    viewModel: ReceiptViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
    walletViewModel: WalletViewModel,
    onRetakeClick: () -> Unit,
    onScanAnotherClick: () -> Unit,
    onConfirmSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
    val categoryUiState by categoryViewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn = authUiState.isLoggedIn
    val context = LocalContext.current
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    val walletOptions = walletState.wallets.map { it.id to it.name }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            categoryViewModel.loadCategories("Expense", true)
            walletViewModel.loadWallets()
        }
    }

    LaunchedEffect(walletState.wallets) {
        if (selectedWalletId == null) selectedWalletId = walletState.wallets.firstOrNull()?.id
    }

    Column(
        modifier = Modifier
            .background(CandyBg)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppTopBar()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Confirm Receipt",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandGreenDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Review the extracted details before saving.",
            fontSize = 14.sp,
            color = TextLight,
            textAlign = TextAlign.Center
        )

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = BrandGreenDark,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analyzing receipt with AI...",
                    fontSize = 13.sp,
                    color = BrandGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Error: ${uiState.errorMessage}",
                fontSize = 13.sp,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }

        if (uiState.requiresClarification) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LemonDark.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Review suggested details",
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    if (uiState.duplicateCandidates.isNotEmpty()) {
                        Text(
                            text = "Possible duplicate: ${uiState.duplicateCandidates.size} similar transaction(s) already exist.",
                            fontSize = 12.sp,
                            color = PrimaryRed
                        )
                    }
                    val issues = uiState.ambiguities
                        .joinToString { "${it.field}: ${it.code.lowercase().replace('_', ' ')}" }
                    if (issues.isNotBlank()) {
                        Text(text = issues, fontSize = 12.sp, color = TextDark)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        ReceiptImageSection(
            imageUri = uiState.imageUri,
            onRetake = onRetakeClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        StoreNameCard(
            storeName = uiState.storeName,
            onValueChange = { viewModel.updateStoreName(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DateAndCategoryRow(
            dateText = uiState.date,
            currentCategoryId = uiState.categoryId,
            categoryCandidates = uiState.categoryCandidates,
            onCategoryChange = { categoryId, category ->
                viewModel.updateCategory(categoryId, category)
            },
            categoryViewModel = categoryViewModel,
            isLoggedIn = isLoggedIn
        )

        categoryUiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            NetworkErrorBanner(
                message = error,
                onRetry = { categoryViewModel.loadCategories("Expense", isLoggedIn) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TotalAmountCard(
            amount = uiState.amount,
            currency = uiState.currency,
            onValueChange = { viewModel.updateAmount(it) }
        )

        ReceiptFactsCard(
            subtotal = uiState.subtotal,
            tax = uiState.tax,
            discount = uiState.discount,
            tip = uiState.tip,
            paymentMethod = uiState.paymentMethod,
            currency = uiState.currency
        )

        if (isLoggedIn && walletOptions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            WalletSelector(
                selectedWalletId = selectedWalletId,
                walletOptions = walletOptions,
                onWalletSelect = { selectedWalletId = it }
            )
        }

        walletState.error?.let { error ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(error, color = PrimaryRed, modifier = Modifier.weight(1f))
                TextButton(onClick = walletViewModel::loadWallets) {
                    Text(stringResource(R.string.retry))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .dashedBorder(cornerRadius = 26.dp, color = DashedBorderColor)
                .clip(RoundedCornerShape(26.dp))
                .clickable { onScanAnotherClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = null,
                    tint = BrandGreenDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan another receipt",
                    color = BrandGreenDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.extractionId != null && uiState.categoryId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.updateRememberAiCategory(!uiState.rememberAiCategory)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.rememberAiCategory,
                    onCheckedChange = viewModel::updateRememberAiCategory
                )
                Text(
                    text = "Remember this merchant and category",
                    color = TextDark,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.confirmAndSave(
                    categoryViewModel = categoryViewModel,
                    selectedWalletId = selectedWalletId,
                    isLoggedIn = isLoggedIn,
                    onSuccess = onConfirmSuccess,
                    onFailure = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            enabled = !uiState.isSubmitting
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Confirm & Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Confirm",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            }
        }
    }
}

/**
 * Khung ảnh hóa đơn và nút Retake
 */
