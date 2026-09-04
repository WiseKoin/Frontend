package com.wisekoin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wisekoin.navigation.Screen
import com.wisekoin.ui.components.AppTopBar
import com.wisekoin.ui.components.WiseKoinBottomNavigation
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
            modifier = Modifier.size(136.dp),
            contentAlignment = Alignment.Center
        ) {
            // Avatar image container
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .neomorphicCardShadow(CircleShape, elevation = 8.dp)
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
                    .clip(CircleShape)
                    .background(WiseKoinTheme.colors.PrimaryContainer)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .neomorphicCardShadow(CircleShape, elevation = 4.dp)
                    .clickable {
                        Toast.makeText(context, "Chỉnh sửa ảnh đại diện", Toast.LENGTH_SHORT).show()
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
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = WiseKoinTheme.colors.OnSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = uiState.email,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = WiseKoinTheme.colors.OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3. Badges (Pro Member & Level 12)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Badge 1: Pro Member
            BadgeCard(
                modifier = Modifier.weight(1f),
                title = "Pro Member",
                icon = Icons.Default.WorkspacePremium,
                iconTint = WiseKoinTheme.colors.Primary,
                onClick = { Toast.makeText(context, "Gói thành viên Pro", Toast.LENGTH_SHORT).show() }
            )

            // Badge 2: Level 12
            BadgeCard(
                modifier = Modifier.weight(1f),
                title = "Level 12",
                icon = Icons.Default.Star,
                iconTint = Color(0xFFEAB308),
                onClick = { Toast.makeText(context, "Cấp độ tiết kiệm: Level 12", Toast.LENGTH_SHORT).show() }
            )
        }

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

            PreferenceItem(
                title = "Account Settings",
                icon = Icons.Default.ManageAccounts,
                iconTint = WiseKoinTheme.colors.Primary,
                onClick = { Toast.makeText(context, "Cài đặt tài khoản", Toast.LENGTH_SHORT).show() }
            )

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
                onClick = { Toast.makeText(context, "Trung tâm trợ giúp", Toast.LENGTH_SHORT).show() }
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
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WiseKoinTheme.colors.Surface,
                    contentColor = PrimaryRed
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
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
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
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
                    .clip(CircleShape)
                    .background(WiseKoinTheme.colors.Surface),
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
            .clip(RoundedCornerShape(28.dp))
            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
            .neomorphicCardShadow(RoundedCornerShape(28.dp), elevation = 6.dp)
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
