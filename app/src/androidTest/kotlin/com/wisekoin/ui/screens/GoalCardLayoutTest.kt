package com.wisekoin.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wisekoin.data.model.GoalResponseDto
import com.wisekoin.ui.components.LocalCurrencyCode
import com.wisekoin.ui.theme.WiseKoinTheme
import java.math.BigDecimal
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalCardLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longGoalNameDoesNotOverlapDeadline() {
        val goalName = "Tai nghe chụp tai Lenovo có tên rất dài"
        composeRule.setContent {
            WiseKoinTheme {
                CompositionLocalProvider(LocalCurrencyCode provides "USD") {
                    Box(Modifier.width(340.dp)) {
                        GoalCardItem(
                            goal = GoalResponseDto(
                                id = 1,
                                name = goalName,
                                targetAmount = BigDecimal("200000"),
                                currentAmount = BigDecimal.ZERO,
                                remainingAmount = BigDecimal("200000"),
                                deadline = "2026-11-01",
                                status = "ACTIVE",
                                progressPercent = BigDecimal.ZERO
                            ),
                            onClick = {}
                        )
                    }
                }
            }
        }

        val nameBounds = composeRule
            .onNodeWithText(goalName, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val deadlineBounds = composeRule
            .onNodeWithText("November 2026", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(nameBounds.right <= deadlineBounds.left)
    }
}
