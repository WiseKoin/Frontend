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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

