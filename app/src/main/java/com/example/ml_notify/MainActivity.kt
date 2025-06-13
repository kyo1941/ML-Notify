package com.example.ml_notify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ml_notify.ui.main.MainScreen
import com.example.ml_notify.ui.main.TaskDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainScreen(navController = navController) }

                    composable(
                        route = "taskDetail/{processId}",
                        arguments = listOf(navArgument("processId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val processId = backStackEntry.arguments?.getString("processId")
                            ?: throw IllegalArgumentException("[ERROR] processId is null")
                        TaskDetailScreen(navController = navController, processId = processId)
                    }
                }
            }
        }
    }
}
