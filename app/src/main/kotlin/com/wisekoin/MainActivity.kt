package com.wisekoin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wisekoin.di.AppContainer
import com.wisekoin.navigation.WiseKoinApp
import com.wisekoin.ui.theme.CandyNeomorphicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = AppContainer(applicationContext)
        enableEdgeToEdge()
        setContent {
            CandyNeomorphicTheme {
                WiseKoinApp(container = container)
            }
        }
    }
}
