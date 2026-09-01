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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisekoin.ui.theme.*

data class GoalModel(
    val id: String,
    val title: String,
    val deadline: String,
    val currentAmount: Long,
    val targetAmount: Long,
    val percentage: Int,
    val color: Color
)

val sampleGoals = listOf(
    GoalModel("1", "New Laptop (MacBook M3)", "Dec 2025", 18500000L, 35000000L, 52, PeachDark),
    GoalModel("2", "Japan Summer Trip", "Jul 2026", 12000000L, 25000000L, 48, MintDark),
    GoalModel("3", "Emergency Reserve Fund", "Ongoing", 15000000L, 20000000L, 75, LavenderDark)
)

@Composable
fun SavingsGoalsScreen(
    onAddNewGoal: () -> Unit,
    onGoalClick: (GoalModel) -> Unit
) {
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neomorphic(cornerRadius = 32.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryRed)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("TOTAL AMOUNT SAVED", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("45,500,000 VND", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("+2,500K compared to last month", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                    Text("Add Goal", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Goals List
        items(sampleGoals) { goal ->
            GoalCardItem(goal = goal, onClick = { onGoalClick(goal) })
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GoalCardItem(
    goal: GoalModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .neomorphic(cornerRadius = 24.dp)
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
                            .background(goal.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(goal.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Text(goal.deadline, fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${goal.currentAmount / 1_000_000}M / ${goal.targetAmount / 1_000_000}M VND",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    "${goal.percentage}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryRed
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { goal.percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = goal.color,
                trackColor = Color(0xFFE2E8F0)
            )
        }
    }
}
