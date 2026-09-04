package com.wisekoin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.wisekoin.data.local.TokenManager
import com.wisekoin.data.remote.RetrofitClient
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wisekoin.ui.components.WiseKoinBottomNavigation
import com.wisekoin.ui.screens.*
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.ReceiptViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Profile : Screen("profile")
    object Overview : Screen("overview")
    object Expenses : Screen("expenses")
    object Savings : Screen("savings")
    object Debt : Screen("debt")
    object AddTransaction : Screen("add_transaction")
    object ScanReceipt : Screen("scan_receipt")
    object ConfirmReceipt : Screen("confirm_receipt") // Route mới
    object VoiceRecording : Screen("voice_recording")
    object CreateGoal : Screen("create_goal")
    object AddFunds : Screen("add_funds")
}

@Composable
fun WiseKoinApp(tokenManager: TokenManager? = null) {
    val context = LocalContext.current
    val effectiveTokenManager = remember(tokenManager) { tokenManager ?: TokenManager(context) }
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Overview.route
    val authViewModel: AuthViewModel = viewModel()

    LaunchedEffect(effectiveTokenManager) {
        RetrofitClient.init(effectiveTokenManager)
        authViewModel.setTokenManager(effectiveTokenManager)
    }

    // Chỉ hiển thị BottomBar ở 4 màn hình chính, ẩn ở ConfirmReceipt
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
            authViewModel = authViewModel,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun WiseKoinNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    // Shared ViewModel cho luồng Scan và Confirm
    val receiptViewModel: ReceiptViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Overview.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Overview.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { },
                onForgotPasswordClick = { }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Overview.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Overview.route) {
            OverviewScreen(
                onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onSeeAllTransactions = { navController.navigate(Screen.Expenses.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
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

        // Quét hóa đơn
        composable(Screen.ScanReceipt.route) {
            ScanReceiptScreen(
                onClose = { navController.popBackStack() },
                onNavigateToConfirm = {
                    navController.navigate(Screen.ConfirmReceipt.route)
                },
                viewModel = receiptViewModel
            )
        }

        // Xác nhận hóa đơn
        composable(Screen.ConfirmReceipt.route) {
            ConfirmReceiptScreen(
                viewModel = receiptViewModel,
                onRetakeClick = {
                    navController.popBackStack()
                },
                onScanAnotherClick = {
                    receiptViewModel.resetState()
                    navController.popBackStack()
                },
                onConfirmSuccess = {
                    // Chuyển về màn hình Tổng quan sau khi lưu thành công
                    navController.navigate(Screen.Overview.route) {
                        popUpTo(Screen.Overview.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.VoiceRecording.route) {
            VoiceRecordingScreen(onClose = { navController.popBackStack() })
        }

        composable(Screen.CreateGoal.route) {
            CreateGoalScreen(onClose = { navController.popBackStack() })
        }

        composable(Screen.AddFunds.route) {
            AddFundsScreen(onClose = { navController.popBackStack() })
        }
    }
}