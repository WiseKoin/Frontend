package com.wisekoin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wisekoin.ui.components.WiseKoinBottomNavigation
import com.wisekoin.ui.screens.*

sealed class Screen(val route: String) {
    object Overview : Screen("overview")
    object Expenses : Screen("expenses")
    object Savings : Screen("savings")
    object Debt : Screen("debt")
    object AddTransaction : Screen("add_transaction")
    object ScanReceipt : Screen("scan_receipt")
    object VoiceRecording : Screen("voice_recording")
    object CreateGoal : Screen("create_goal")
    object AddFunds : Screen("add_funds")
}

@Composable
fun WiseKoinApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Overview.route

    val showBottomBar = currentRoute in listOf(
        Screen.Overview.route,
        Screen.Expenses.route,
        Screen.Savings.route,
        Screen.Debt.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                WiseKoinBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Overview.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        WiseKoinNavGraph(
            navController = navController,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun WiseKoinNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Overview.route,
        modifier = modifier
    ) {
        composable(Screen.Overview.route) {
            OverviewScreen(
                onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onSeeAllTransactions = { navController.navigate(Screen.Expenses.route) }
            )
        }
        composable(Screen.Expenses.route) {
            ExpensesScreen(onAddExpense = { navController.navigate(Screen.AddTransaction.route) })
        }
        composable(Screen.Savings.route) {
            SavingsGoalsScreen(
                onAddNewGoal = { navController.navigate(Screen.CreateGoal.route) },
                onGoalClick = { navController.navigate(Screen.AddFunds.route) }
            )
        }
        composable(Screen.Debt.route) {
            DebtManagementScreen(onAddDebt = { navController.navigate(Screen.AddTransaction.route) })
        }
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onClose = { navController.popBackStack() },
                onVoiceAdd = { navController.navigate(Screen.VoiceRecording.route) },
                onScanReceipt = { navController.navigate(Screen.ScanReceipt.route) }
            )
        }
        composable(Screen.VoiceRecording.route) {
            VoiceRecordingScreen(onClose = { navController.popBackStack() })
        }
        composable(Screen.ScanReceipt.route) {
            ScanReceiptScreen(onClose = { navController.popBackStack() })
        }
        composable(Screen.CreateGoal.route) {
            CreateGoalScreen(onClose = { navController.popBackStack() })
        }
        composable(Screen.AddFunds.route) {
            AddFundsScreen(onClose = { navController.popBackStack() })
        }
    }
}
