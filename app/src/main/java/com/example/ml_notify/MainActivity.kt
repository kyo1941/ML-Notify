package com.example.ml_notify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
