package com.wisekoin.di

import android.content.Context
import com.wisekoin.data.local.SessionTokenStore
import com.wisekoin.data.local.TokenManager
import com.wisekoin.data.local.BankNotificationStoreProvider
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.data.repository.AccountSecurityRepository
import com.wisekoin.data.repository.AuthRepository
import com.wisekoin.data.repository.CategoryRepository
import com.wisekoin.data.repository.DebtRepository
import com.wisekoin.data.repository.GoalRepository
import com.wisekoin.data.repository.NetworkAccountSecurityRepository
import com.wisekoin.data.repository.NetworkAuthRepository
import com.wisekoin.data.repository.NetworkCategoryRepository
import com.wisekoin.data.repository.NetworkDebtRepository
import com.wisekoin.data.repository.NetworkGoalRepository
import com.wisekoin.data.repository.NetworkReceiptRepository
import com.wisekoin.data.repository.NetworkTransactionRepository
import com.wisekoin.data.repository.NetworkWalletRepository
import com.wisekoin.data.repository.ReceiptRepository
import com.wisekoin.data.repository.TransactionRepository
import com.wisekoin.data.repository.WalletRepository

class SessionExpiryNotifier {
    @Volatile
    private var listener: (() -> Unit)? = null

    fun setListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    fun notifyExpired() {
        listener?.invoke()
    }
}

class AppContainer(context: Context) {
    val tokenStore: SessionTokenStore = TokenManager(context.applicationContext)
    val sessionExpiryNotifier = SessionExpiryNotifier()
    val bankNotificationStore = BankNotificationStoreProvider.get(context.applicationContext)

    private val client = RetrofitClient(tokenStore, sessionExpiryNotifier::notifyExpired)

    val authRepository: AuthRepository = NetworkAuthRepository(client.authApi, client.userApi)
    val accountSecurityRepository: AccountSecurityRepository =
        NetworkAccountSecurityRepository(client.userApi)
    val categoryRepository: CategoryRepository = NetworkCategoryRepository(client.categoryApi)
    val debtRepository: DebtRepository = NetworkDebtRepository(client.debtApi)
    val goalRepository: GoalRepository = NetworkGoalRepository(client.goalApi)
    val receiptRepository: ReceiptRepository = NetworkReceiptRepository(client.receiptApi)
    val transactionRepository: TransactionRepository =
        NetworkTransactionRepository(client.transactionApi)
    val walletRepository: WalletRepository = NetworkWalletRepository(client.walletApi)
}
