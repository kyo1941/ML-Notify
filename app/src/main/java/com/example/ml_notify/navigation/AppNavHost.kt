package com.example.ml_notify.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ml_notify.ui.main.MainScreen
import com.example.ml_notify.ui.task_detail.TaskDetailScreen
import com.example.ml_notify.ui.main.MainViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    NavHost(navController = navController, startDestination = AppRoutes.MAIN_SCREEN) {
        composable(AppRoutes.MAIN_SCREEN) {
            MainScreen(navController = navController)
        }
        composable(
            route = "${AppRoutes.TASK_DETAIL_SCREEN}/{${AppRoutes.TASK_DETAIL_ARGUMENT}}",
            arguments = listOf(navArgument(AppRoutes.TASK_DETAIL_ARGUMENT) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val processId = backStackEntry.arguments?.getString(AppRoutes.TASK_DETAIL_ARGUMENT)
            LaunchedEffect(processId) {
                if (processId == null) {
                    mainViewModel.showSnackbar("processIdが取得できませんでした")
                    navController.popBackStack(AppRoutes.MAIN_SCREEN, false)
                }
            }
            if (processId != null) {
                TaskDetailScreen(navController = navController, processId = processId)
            }
        }
    }
}