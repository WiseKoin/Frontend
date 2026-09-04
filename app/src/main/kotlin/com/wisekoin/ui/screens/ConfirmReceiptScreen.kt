package com.wisekoin.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.ReceiptViewModel

@Composable
fun ConfirmReceiptScreen(
    viewModel: ReceiptViewModel,
    onRetakeClick: () -> Unit,
    onScanAnotherClick: () -> Unit,
    onConfirmSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CandyBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Logo + Notification + Avatar
            ConfirmReceiptTopBar()

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Tiêu đề màn hình
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

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Khung hiển thị ảnh hóa đơn & Nút Retake
            ReceiptImageSection(
                imageUri = uiState.imageUri,
                onRetake = onRetakeClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Card Store Name (Inline Editing)
            StoreNameCard(
                storeName = uiState.storeName,
                onValueChange = { viewModel.updateStoreName(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Hàng 2 cột: Date & Category
            DateAndCategoryRow(
                dateText = uiState.date,
                currentCategory = uiState.category,
                onCategoryChange = { viewModel.updateCategory(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Card Total Amount (Inline Editing)
            TotalAmountCard(
                amount = uiState.amount,
                onValueChange = { viewModel.updateAmount(it) }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 7. Nút viền nét đứt: Scan another receipt
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

            // 8. Nút chính: Confirm & Save
            Button(
                onClick = {
                    viewModel.confirmAndSave(onSuccess = onConfirmSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Top Bar với logo WiseKoin, chuông và avatar
 */
@Composable
private fun ConfirmReceiptTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Tên WiseKoin
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrandGreenDark)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WiseKoin",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BrandGreenDark
            )
        }

        // Cụm icon góc phải: Chuông + Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .neomorphicCircle(
                        shadowColor = Color.Black.copy(alpha = 0.08f),
                        lightColor = Color.White,
                        offset = 2.dp,
                        blur = 4.dp
                    )
                    .clip(CircleShape)
                    .background(CandyBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notification",
                    tint = TextDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MintDark.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = BrandGreenDark,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Khung ảnh hóa đơn và nút Retake
 */
@Composable
private fun ReceiptImageSection(
    imageUri: Uri?,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Khung ảnh hóa đơn bo tròn
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .neomorphicBox(cornerRadius = 28.dp, elevation = 5.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CandyBg)
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Receipt Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Ảnh placeholder dự phòng nếu chưa có ảnh chụp
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1554415707-9e4426c62c76?auto=format&fit=crop&w=600&q=80",
                    contentDescription = "Placeholder Receipt",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Nút Retake nổi
        Box(
            modifier = Modifier
                .neomorphicBox(cornerRadius = 20.dp, elevation = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable { onRetake() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retake",
                    tint = BrandGreenDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Retake",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreenDark
                )
            }
        }
    }
}

/**
 * Thẻ tên cửa hàng (Store Name) với Inline Editing
 */
@Composable
private fun StoreNameCard(
    storeName: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp, elevation = 5.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STORE NAME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Store Name",
                    tint = BrandGreenDark,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ô nhập liệu lõm (Sunken Inset)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .neomorphicInset(cornerRadius = 16.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = storeName,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    ),
                    cursorBrush = SolidColor(BrandGreenDark),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Hàng 2 cột Date và Category
 */
@Composable
private fun DateAndCategoryRow(
    dateText: String,
    currentCategory: String,
    onCategoryChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Dining", "Transport", "Shopping", "Entertainment", "General")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cột 1: Date Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(115.dp)
                .neomorphicBox(cornerRadius = 24.dp, elevation = 5.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CandyBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = TextLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = dateText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    lineHeight = 18.sp
                )
            }
        }

        // Cột 2: Category Card
        Card(
            modifier = Modifier
                .weight(1.1f)
                .height(115.dp)
                .neomorphicBox(cornerRadius = 24.dp, elevation = 5.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CandyBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TextLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Badge danh mục
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(LemonLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = currentCategory,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                // Dropdown chọn nhanh danh mục
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                onCategoryChange(cat)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thẻ số tiền (Total Amount) với Inline Editing
 */
@Composable
private fun TotalAmountCard(
    amount: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp, elevation = 5.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL AMOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Amount",
                    tint = BrandGreenDark,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ô lõm bao quanh số tiền
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .neomorphicInset(cornerRadius = 18.dp)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = amount,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandGreenDark
                        ),
                        cursorBrush = SolidColor(BrandGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Đơn vị tiền tệ bên ngoài ô lõm
                Text(
                    text = "VND",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
            }
        }
    }
}