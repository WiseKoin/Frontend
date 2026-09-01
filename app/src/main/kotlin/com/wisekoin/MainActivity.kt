package com.wisekoin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wisekoin.navigation.WiseKoinApp
import com.wisekoin.ui.theme.CandyNeomorphicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CandyNeomorphicTheme {
                WiseKoinApp()
            }
        }
    }
}
