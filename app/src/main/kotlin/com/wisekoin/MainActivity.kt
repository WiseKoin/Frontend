package com.wisekoin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wisekoin.data.local.TokenManager
import com.wisekoin.data.remote.RetrofitClient
import com.wisekoin.navigation.WiseKoinApp
import com.wisekoin.ui.theme.CandyNeomorphicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)
        enableEdgeToEdge()
        setContent {
            CandyNeomorphicTheme {
                WiseKoinApp(tokenManager = tokenManager)
            }
        }
    }
}
