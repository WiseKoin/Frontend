package com.wisekoin.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wisekoin.viewmodel.AccountSecurityViewModel
import com.wisekoin.viewmodel.AuthViewModel
import com.wisekoin.viewmodel.BankNotificationViewModel
import com.wisekoin.viewmodel.CategoryViewModel
import com.wisekoin.viewmodel.DebtViewModel
import com.wisekoin.viewmodel.FeedbackViewModel
import com.wisekoin.viewmodel.GoalViewModel
import com.wisekoin.viewmodel.PasswordRecoveryViewModel
import com.wisekoin.viewmodel.ReceiptViewModel
import com.wisekoin.viewmodel.TransactionEntryViewModel
import com.wisekoin.viewmodel.VoiceViewModel
import com.wisekoin.viewmodel.WalletViewModel

class WiseKoinViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(container.authRepository, container.tokenStore)
        modelClass.isAssignableFrom(AccountSecurityViewModel::class.java) ->
            AccountSecurityViewModel(container.accountSecurityRepository)
        modelClass.isAssignableFrom(BankNotificationViewModel::class.java) ->
            BankNotificationViewModel(
                container.bankNotificationStore,
                container.bankNotificationSuggestionRepository,
                container.bankNotificationSyncer,
                container.walletRepository,
                container.categoryRepository,
                container.transactionRepository
            )
        modelClass.isAssignableFrom(CategoryViewModel::class.java) ->
            CategoryViewModel(container.categoryRepository)
        modelClass.isAssignableFrom(DebtViewModel::class.java) ->
            DebtViewModel(container.debtRepository)
        modelClass.isAssignableFrom(FeedbackViewModel::class.java) ->
            FeedbackViewModel(container.feedbackRepository)
        modelClass.isAssignableFrom(GoalViewModel::class.java) ->
            GoalViewModel(container.goalRepository)
        modelClass.isAssignableFrom(PasswordRecoveryViewModel::class.java) ->
            PasswordRecoveryViewModel(container.authRepository)
        modelClass.isAssignableFrom(ReceiptViewModel::class.java) ->
            ReceiptViewModel(container.receiptRepository, container.transactionRepository)
        modelClass.isAssignableFrom(TransactionEntryViewModel::class.java) ->
            TransactionEntryViewModel(
                container.transactionRepository,
                container.debtRepository,
                container.walletRepository
            )
        modelClass.isAssignableFrom(VoiceViewModel::class.java) ->
            VoiceViewModel(container.receiptRepository, container.transactionRepository)
        modelClass.isAssignableFrom(WalletViewModel::class.java) ->
            WalletViewModel(container.walletRepository, container.transactionRepository)
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    } as T
}
