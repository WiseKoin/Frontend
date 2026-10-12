package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.data.model.GoalResponseDto
import com.wisekoin.ui.theme.*
import com.wisekoin.viewmodel.GoalViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun SavingsGoalsScreen(
    goalViewModel: GoalViewModel = viewModel(),
    onAddNewGoal: () -> Unit,
    onGoalClick: (Long) -> Unit
) {
    val uiState by goalViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        goalViewModel.loadGoals()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CandyBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text("Savings Goals", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextDark)
            Text("Track and manage your savings plans based on specific goals.", fontSize = 14.sp, color = TextLight)
        }

        // Total Saved Banner
        item {
            TotalSavedBanner(totalSaved = goalViewModel.totalSaved)
        }

        // Action header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Active Goals",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Button(
                    onClick = onAddNewGoal,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Goal", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Loading State
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            }
        }

        // Error State
        if (uiState.errorMessage != null) {
            item {
                ErrorBanner(message = uiState.errorMessage!!, onRetry = { goalViewModel.loadGoals() })
            }
        }

        // Empty State
        if (!uiState.isLoading && uiState.errorMessage == null && goalViewModel.activeGoals.isEmpty()) {
            item {
                EmptyGoalsBanner(onAddNewGoal = onAddNewGoal)
            }
        }

        // Goals List
        items(goalViewModel.activeGoals, key = { it.id }) { goal ->
            GoalCardItem(goal = goal, onClick = { onGoalClick(goal.id) })
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TotalSavedBanner(totalSaved: BigDecimal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 32.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("TOTAL AMOUNT SAVED", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(totalSaved.formatSavingsVND(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Across all your savings goals", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Failed to load goals", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryRed)
            Text(message, fontSize = 12.sp, color = TextLight)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("Retry", fontWeight = FontWeight.Bold, color = PrimaryRed)
            }
        }
    }
}

@Composable
private fun EmptyGoalsBanner(onAddNewGoal: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MintDark.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Savings,
                    contentDescription = null,
                    tint = MintDark,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("No savings goals yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(4.dp))
            Text(
                "Start your first goal and begin saving today!",
                fontSize = 13.sp,
                color = TextLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddNewGoal,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Create Goal", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun GoalCardItem(
    goal: GoalResponseDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphicBox(cornerRadius = 24.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(goal.goalColor())
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(goal.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Text(goal.deadlineText(), fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${goal.currentAmount.formatCompactVND()} / ${goal.targetAmount.formatCompactVND()} VND",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    "${goal.progressPercent.stripTrailingZeros().toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryRed
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { goal.progressPercent.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = goal.goalColor(),
                trackColor = Color(0xFFE2E8F0)
            )
        }
    }
}

private fun GoalResponseDto.goalColor(): Color {
    return when ((id % 3).toInt()) {
        0 -> PeachDark
        1 -> MintDark
        else -> LavenderDark
    }
}

fun GoalResponseDto.deadlineText(): String {
    val d = deadline ?: return "Ongoing"
    return try {
        val date = java.time.LocalDate.parse(d)
        "${date.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }} ${date.year}"
    } catch (e: Exception) {
        d
    }
}

fun BigDecimal.formatCompactVND(): String {
    return when {
        this >= BigDecimal(1_000_000_000L) -> {
            val value = this.divide(BigDecimal(1_000_000_000L), 1, RoundingMode.HALF_UP)
            "${value.stripTrailingZeros().toPlainString()}B"
        }
        this >= BigDecimal(1_000_000L) -> {
            val value = this.divide(BigDecimal(1_000_000L), 1, RoundingMode.HALF_UP)
            "${value.stripTrailingZeros().toPlainString()}M"
        }
        this >= BigDecimal(1000) -> {
            val value = this.divide(BigDecimal(1000), 1, RoundingMode.HALF_UP)
            "${value.stripTrailingZeros().toPlainString()}K"
        }
        else -> this.stripTrailingZeros().toPlainString()
    }
}

fun BigDecimal.formatSavingsVND(): String {
    return "%,d VND".format(this.toLong())
}
