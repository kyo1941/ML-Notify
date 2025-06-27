package com.example.ml_notify

import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.google.firebase.messaging.FirebaseMessaging

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FCMトークンを取得してログに表示
        retrieveAndLogFCMToken()

        setContent {
            val view = LocalView.current
            val window = remember(view) { (view.context as ComponentActivity).window }
            val insetsController = remember(window, view) { WindowCompat.getInsetsController(window, view) }
            SideEffect {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                insetsController.isAppearanceLightStatusBars = true
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


    private fun retrieveAndLogFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "FCMトークンの取得に失敗しました", task.exception)
                return@addOnCompleteListener
            }

            // トークンを取得してログに出力
            val token = task.result

            // 開発用に大きく目立つように出力
            Log.i(TAG, "==========================================")
            Log.i(TAG, "FCM Device Token: $token")
            Log.i(TAG, "==========================================")
        }
    }
}
