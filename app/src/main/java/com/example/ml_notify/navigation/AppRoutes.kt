package com.example.ml_notify.navigation

import android.net.Uri

object AppRoutes {
    const val MAIN_SCREEN = "main"
    const val TASK_DETAIL_SCREEN = "taskDetail"
    const val TASK_DETAIL_ARGUMENT = "processId"

    object DeepLink {
        const val SCHEME = "app"
        const val AUTHORITY = "ml_notify"

        // TaskDetailのURIパターンを生成するヘルパーメソッド
        fun getTaskDetailUriPattern(): String {
            return "$SCHEME://$AUTHORITY/$TASK_DETAIL_SCREEN/{$TASK_DETAIL_ARGUMENT}"
        }

        // 特定のタスクIDに対するDeepLinkを生成するヘルパーメソッド
        fun buildTaskDetailUri(processId: String): Uri {
            return Uri.Builder()
                .scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TASK_DETAIL_SCREEN)
                .appendPath(processId)
                .build()
        }
    }
}