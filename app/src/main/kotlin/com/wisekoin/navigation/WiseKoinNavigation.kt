package com.wisekoin.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisekoin.di.AppContainer
import com.wisekoin.di.WiseKoinViewModelFactory
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wisekoin.ui.components.WiseKoinBottomBar
import com.wisekoin.ui.screens.*
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.AccountSecurityViewModel
import com.wisekoin.viewmodel.GoalViewModel
import com.wisekoin.viewmodel.ReceiptViewModel
import com.wisekoin.viewmodel.VoiceViewModel
import com.wisekoin.viewmodel.WalletViewModel
import com.wisekoin.viewmodel.SessionStatus
import com.wisekoin.R

import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.DebtViewModel
import com.wisekoin.viewmodel.TransactionEntryViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object AccountSecurity : Screen("account_security")
    object ChangePassword : Screen("change_password")
    object DeleteAccount : Screen("delete_account")
    object Overview : Screen("overview")
    object Expenses : Screen("expenses")
    object Savings : Screen("savings")
    object Debt : Screen("debt")
    object AddTransaction : Screen("add_transaction")
    object ScanReceipt : Screen("scan_receipt")
    object ConfirmReceipt : Screen("confirm_receipt") // Route mới
    object VoiceRecording : Screen("voice_recording")
    object CreateGoal : Screen("create_goal")
    object AddFunds : Screen("add_funds/{goalId}") {
        fun createRoute(goalId: Long): String = "add_funds/$goalId"
    }
}

@Composable
fun WiseKoinApp(container: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Overview.route
    val viewModelFactory = remember(container) { WiseKoinViewModelFactory(container) }
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(container, authViewModel) {
        container.sessionExpiryNotifier.setListener(authViewModel::onSessionExpired)
        onDispose { container.sessionExpiryNotifier.setListener(null) }
    }

    LaunchedEffect(authUiState.sessionExpiryVersion) {
        if (authUiState.sessionExpiryVersion > 0 && navController.currentBackStackEntry != null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Only show bottom bar in listed screens
    val showBottomBar = authUiState.sessionStatus == SessionStatus.AUTHENTICATED &&
        currentRoute in listOf(
        Screen.Overview.route,
        Screen.Expenses.route,
        Screen.Savings.route,
        Screen.Debt.route,
        Screen.Profile.route,
        Screen.AddTransaction.route
    )

    if (authUiState.sessionStatus == SessionStatus.RESOLVING ||
        authUiState.sessionStatus == SessionStatus.ERROR
    ) {
        SessionResolutionScreen(
            failed = authUiState.sessionStatus == SessionStatus.ERROR,
            onRetry = authViewModel::retrySessionResolution
        )
        return
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                WiseKoinBottomBar(
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
            viewModelFactory = viewModelFactory,
            startDestination = if (authUiState.sessionExpiryVersion > 0) {
                Screen.Login.route
            } else {
                Screen.Overview.route
            },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun WiseKoinNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    viewModelFactory: WiseKoinViewModelFactory,
    startDestination: String = Screen.Overview.route,
    modifier: Modifier = Modifier
) {
    // Shared ViewModel cho luồng Scan và Confirm
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)
    val receiptViewModel: ReceiptViewModel = viewModel(factory = viewModelFactory)
    val goalViewModel: GoalViewModel = viewModel(factory = viewModelFactory)
    val transactionEntryViewModel: TransactionEntryViewModel = viewModel(factory = viewModelFactory)
    val accountSecurityViewModel: AccountSecurityViewModel = viewModel(factory = viewModelFactory)

    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                onNavigateToSignUp = {
                    navController.navigate(Screen.Register.route)
                },
                onForgotPasswordClick = { }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Overview.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onLogoutClick = {
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Overview.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route){
                        popUpTo(Screen.Overview.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAccountSecurityClick = {
                    navController.navigate(Screen.AccountSecurity.route)
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

        composable(Screen.AccountSecurity.route) {
            AccountSecurityScreen(
                email = authUiState.email,
                onBack = { navController.popBackStack() },
                onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onDeleteAccount = { navController.navigate(Screen.DeleteAccount.route) }
            )
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                viewModel = accountSecurityViewModel,
                onBack = { navController.popBackStack() },
                onPasswordChanged = {
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.DeleteAccount.route) {
            DeleteAccountScreen(
                viewModel = accountSecurityViewModel,
                onBack = { navController.popBackStack() },
                onAccountDeleted = {
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Overview.route) {
            val walletViewModel: WalletViewModel = viewModel(factory = viewModelFactory)
            OverviewScreen(
                userName = authUiState.name,
                isLoggedIn = authUiState.isLoggedIn,
                walletViewModel = walletViewModel,
                onNavigateToAddTransaction = {
                    val destination = if (authUiState.isLoggedIn) {
                        Screen.AddTransaction.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(destination){
                        popUpTo(Screen.Overview.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                 },
                onSeeAllTransactions = {
                    navController.navigate(
                        if (authUiState.isLoggedIn) Screen.Expenses.route else Screen.Login.route
                    )
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Overview.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Expenses.route) {
            val walletViewModel: WalletViewModel = viewModel(factory = viewModelFactory)
            ExpensesScreen(
                walletViewModel = walletViewModel,
                categoryViewModel = categoryViewModel,
                isLoggedIn = authUiState.isLoggedIn
            )
        }

        composable(Screen.Savings.route) {
            SavingsGoalsScreen(
                goalViewModel = goalViewModel,
                onAddNewGoal = { navController.navigate(Screen.CreateGoal.route) },
                onGoalClick = { goalId ->
                    navController.navigate(Screen.AddFunds.createRoute(goalId))
                }
            )
        }

        composable(Screen.Debt.route) {
            val debtViewModel: DebtViewModel = viewModel(factory = viewModelFactory)
            val walletViewModel: WalletViewModel = viewModel(factory = viewModelFactory)
            DebtManagementScreen(
                debtViewModel = debtViewModel,
                categoryViewModel = categoryViewModel,
                walletViewModel = walletViewModel,
                isLoggedIn = authUiState.isLoggedIn,
                onAddDebt = { navController.navigate(Screen.AddTransaction.route){
                        popUpTo(Screen.Overview.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onClose = { navController.popBackStack() },
                onVoiceAdd = { navController.navigate(Screen.VoiceRecording.route) },
                onScanReceipt = { navController.navigate(Screen.ScanReceipt.route) },
                onConfirmSuccess = {
                    // Chuyển về màn hình Tổng quan sau khi lưu thành công
                    navController.navigate(Screen.Overview.route) {
                        popUpTo(Screen.Overview.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onDebtCreated = {
                    navController.navigate(Screen.Debt.route) {
                        popUpTo(Screen.Overview.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Overview.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                authViewModel = authViewModel,
                categoryViewModel = categoryViewModel,
                transactionEntryViewModel = transactionEntryViewModel
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
            val walletViewModel: WalletViewModel = viewModel(factory = viewModelFactory)
            ConfirmReceiptScreen(
                viewModel = receiptViewModel,
                authViewModel = authViewModel,
                categoryViewModel = categoryViewModel,
                walletViewModel = walletViewModel,
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
            val walletViewModel: WalletViewModel = viewModel(factory = viewModelFactory)
            val voiceViewModel: VoiceViewModel = viewModel(factory = viewModelFactory)
            VoiceRecordingScreen(
                onClose = { navController.popBackStack() },
                onConfirmSuccess = {
                    navController.navigate(Screen.Overview.route) {
                        popUpTo(Screen.Overview.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                voiceViewModel = voiceViewModel,
                authViewModel = authViewModel,
                categoryViewModel = categoryViewModel,
                walletViewModel = walletViewModel
            )
        }

        composable(Screen.CreateGoal.route) {
            CreateGoalScreen(
                goalViewModel = goalViewModel,
                onClose = { navController.popBackStack() },
                onGoalCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddFunds.route,
            arguments = listOf(navArgument("goalId") { type = NavType.LongType })
        ) { backStackEntry ->
            val walletViewModel: WalletViewModel = viewModel(factory = viewModelFactory)
            GoalFundingScreen(
                goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L,
                goalViewModel = goalViewModel,
                walletViewModel = walletViewModel,
                onClose = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun SessionResolutionScreen(failed: Boolean, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (failed) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.session_check_failed))
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        } else {
            CircularProgressIndicator()
        }
    }
}
