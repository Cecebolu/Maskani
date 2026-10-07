package com.spit91.maskani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.spit91.maskani.presentation.auth.SignInViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.spit91.maskani.presentation.navigation.MaskaniApp
import com.spit91.maskani.ui.theme.HomeSpotTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 1. INJECT VIEWMODEL: Hilt automatically creates and binds the ViewModel lifecycle for us
    private val signInViewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Apply your application's global design theme wrapper
            HomeSpotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MaskaniApp()
                }
            }
        }
    }
}
