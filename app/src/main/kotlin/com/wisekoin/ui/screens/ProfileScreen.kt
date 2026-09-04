package com.wisekoin.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wisekoin.navigation.Screen
import com.wisekoin.ui.components.AppTopBar
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onLogoutClick: () -> Unit,
    onLoginClick: () -> Unit = {},
    onTabSelected: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAccountMenu by remember { mutableStateOf(false) }
    var editingField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateAvatar(it.toString()) }
    }

    Column(
        modifier = Modifier
            .background(CandyBg)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppTopBar()

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Avatar 3D with Edit Button
        Box(
            modifier = Modifier
                .size(136.dp),
            contentAlignment = Alignment.Center
        ) {
            // Avatar image container
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .neomorphicCircle(
                        shadowColor = Color(0x66A58BFF),
                        lightColor = Color.White.copy(0.8f),
                        offset = 6.dp,
                        blur = 6.dp
                    )
                    .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uiState.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Edit floating button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
                    .neomorphicCardShadow(CircleShape, elevation = 4.dp)
                    .clip(CircleShape)
                    .background(WiseKoinTheme.colors.PrimaryContainer)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Avatar",
                    tint = WiseKoinTheme.colors.OnPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. User Info
        Text(
            text = uiState.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = WiseKoinTheme.colors.OnSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = uiState.email,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = WiseKoinTheme.colors.OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3. Badges (Pro Member & Level 12)
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 24.dp),
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Badge 1: Pro Member
//            BadgeCard(
//                modifier = Modifier.weight(1f),
//                title = "Pro Member",
//                icon = Icons.Default.WorkspacePremium,
//                iconTint = WiseKoinTheme.colors.Primary,
//                onClick = { Toast.makeText(context, "Gói thành viên Pro", Toast.LENGTH_SHORT).show() }
//            )
//
//            // Badge 2: Level 12
//            BadgeCard(
//                modifier = Modifier.weight(1f),
//                title = "Level 12",
//                icon = Icons.Default.Star,
//                iconTint = Color(0xFFEAB308),
//                onClick = { Toast.makeText(context, "Cấp độ tiết kiệm: Level 12", Toast.LENGTH_SHORT).show() }
//            )
//        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Preferences Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Preferences",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WiseKoinTheme.colors.OnSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            )

            // Bọc PreferenceItem và DropdownMenu trong 1 Box làm điểm neo (anchor)
            Box(modifier = Modifier.fillMaxWidth()) {
                PreferenceItem(
                    title = "Account Settings",
                    icon = Icons.Default.ManageAccounts,
                    iconTint = WiseKoinTheme.colors.Primary,
                    onClick = { showAccountMenu = !showAccountMenu }
                )

                DropdownMenu(
                    expanded = showAccountMenu,
                    onDismissRequest = { showAccountMenu = false },
                    // Bo góc menu cho hài hòa với giao diện
                    shape = RoundedCornerShape(18.dp),
                    containerColor = WiseKoinTheme.colors.Surface,
                    modifier = Modifier
                        .fillMaxWidth(0.75f) // Độ rộng vừa vặn với màn hình
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    // 1. Username
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = WiseKoinTheme.colors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Username", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            showAccountMenu = false
                            editingField = "username"
                            editValue = uiState.name
                        }
                    )

                    // Thanh ngang ngăn cách 1
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = 0.8.dp,
                        color = WiseKoinColors.OnSurfaceVariant.copy(alpha = 0.35f)
                    )

                    // 2. Email
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFF666000),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Email", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            showAccountMenu = false
                            Toast.makeText(context, "Email change not supported", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Thanh ngang ngăn cách 2
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = 0.8.dp,
                        color = WiseKoinColors.OnSurfaceVariant.copy(alpha = 0.35f)
                    )

                    // 3. Password
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            showAccountMenu = false
                            Toast.makeText(context, "Password change not supported", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Thanh ngang ngăn cách 3
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = 0.8.dp,
                        color = WiseKoinColors.OnSurfaceVariant.copy(alpha = 0.35f)
                    )

                    // 4. Phone
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ManageAccounts,
                                    contentDescription = null,
                                    tint = Color(0xFF235347),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Phone", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            showAccountMenu = false
                            editingField = "phone"
                            editValue = uiState.currentUser?.phone ?: ""
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceItem(
                title = "Notifications",
                icon = Icons.Default.NotificationsActive,
                iconTint = Color(0xFF666000),
                onClick = { Toast.makeText(context, "Cài đặt thông báo", Toast.LENGTH_SHORT).show() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceItem(
                title = "Help Center",
                icon = Icons.Default.HelpCenter,
                iconTint = WiseKoinTheme.colors.Outline,
                onClick = { Toast.makeText(context, "Help Center", Toast.LENGTH_SHORT).show() }
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 5. Action Button (Log In / Log Out)
        if (uiState.isLoggedIn) {
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp)
                    .neomorphicBox(cornerRadius = 24.dp, elevation = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WiseKoinTheme.colors.Surface,
                    contentColor = PrimaryRed
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out",
                        tint = PrimaryRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed
                    )
                }
            }
        } else {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp)
                    .neomorphicBox(cornerRadius = 24.dp, elevation = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = "Log In",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log In",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Account Settings Edit Dialog
    if (editingField != null) {
        AlertDialog(
            onDismissRequest = { editingField = null },
            containerColor = WiseKoinTheme.colors.SurfaceContainerLow,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Edit ${editingField?.replaceFirstChar { it.uppercase() }}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WiseKoinTheme.colors.OnSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .neomorphicInset(cornerRadius = 16.dp, elevation = 3.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = if (editingField == "phone") {
                        KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done)
                    } else {
                        KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done)
                    }
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { editingField = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .neomorphicBox(cornerRadius = 20.dp, elevation = 4.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WiseKoinTheme.colors.Surface)
                    ) {
                        Text("Cancel", color = WiseKoinTheme.colors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            val field = editingField!!
                            if (editValue.isBlank()) {
                                Toast.makeText(context, "Please enter a value", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.updateProfile(
                                username = if (field == "username") editValue else uiState.name,
                                phone = if (field == "phone") editValue else uiState.currentUser?.phone ?: "",
                                onSuccess = {
                                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                                    editingField = null
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    editingField = null
                                }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .neomorphicBox(cornerRadius = 20.dp, elevation = 4.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WiseKoinTheme.colors.Primary)
                    ) {
                        Text("Save", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        )
    }
}

@Composable
fun BadgeCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .neomorphicBox(
                cornerRadius = 28.dp,
                elevation = 4.dp,
                darkShadowColor = Color(0x66A58BFF),
                lightShadowColor = Color.White.copy(0.8f),
            )
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
            .neomorphicCardShadow(RoundedCornerShape(28.dp), elevation = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = WiseKoinTheme.colors.SurfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .neomorphicCircle(
                        shadowColor = Color(0x66A58BFF),
                        lightColor = Color.White.copy(0.8f),
                        offset = 4.dp,
                        blur = 4.dp
                    )
                    .clip(CircleShape)
                    .background(WiseKoinColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = WiseKoinTheme.colors.OnSurface
            )
        }
    }
}

@Composable
fun PreferenceItem(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(
                cornerRadius = 28.dp,
                elevation = 4.dp,
                darkShadowColor = Color(0x66A58BFF),
                lightShadowColor = Color.White.copy(0.8f),
            )
            .clip(RoundedCornerShape(28.dp))
            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = WiseKoinTheme.colors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .neomorphicCircle(
                        shadowColor = Color(0x66A58BFF),
                        lightColor = Color.White.copy(0.8f),
                        offset = 4.dp,
                        blur = 4.dp
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(WiseKoinTheme.colors.Surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WiseKoinTheme.colors.OnSurface
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = WiseKoinTheme.colors.OnSurfaceVariant
            )
        }
    }
}
