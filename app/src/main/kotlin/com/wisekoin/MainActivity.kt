package com.wisekoin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wisekoin.di.AppContainer
import com.wisekoin.navigation.WiseKoinApp
import com.wisekoin.ui.theme.CandyNeomorphicTheme

class MainActivity : ComponentActivity() {
    private var bankNotificationCandidateId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bankNotificationCandidateId = intent.bankNotificationCandidateId()
        val container = AppContainer(applicationContext)
        enableEdgeToEdge()
        setContent {
            CandyNeomorphicTheme {
                WiseKoinApp(
                    container = container,
                    notificationCandidateId = bankNotificationCandidateId,
                    onNotificationIntentConsumed = { bankNotificationCandidateId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        bankNotificationCandidateId = intent.bankNotificationCandidateId()
    }
}

private fun Intent.bankNotificationCandidateId(): String? =
    getStringExtra(com.wisekoin.notification.BankNotificationAlertManager.EXTRA_CANDIDATE_ID)
