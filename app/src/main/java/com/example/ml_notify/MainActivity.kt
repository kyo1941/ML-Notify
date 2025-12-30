package com.example.ml_notify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.ml_notify.navigation.AppNavHost
import com.example.ml_notify.ui.main.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val view = LocalView.current
            val window = remember(view) { (view.context as ComponentActivity).window }
            val insetsController = remember(window, view) { WindowCompat.getInsetsController(window, view) }
            val isDarkTheme = isSystemInDarkTheme()
            SideEffect {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                insetsController.isAppearanceLightStatusBars = isDarkTheme.not()
            }

            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                val mainViewModel: MainViewModel = viewModel()
                val navController = rememberNavController()
                AppNavHost(navController = navController, mainViewModel = mainViewModel)
            }
        }
    }
}