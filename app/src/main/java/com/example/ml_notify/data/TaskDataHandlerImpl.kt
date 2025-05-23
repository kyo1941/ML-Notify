package com.example.ml_notify.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ml_notify.R
import com.example.ml_notify.domain.TaskDataHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton // アプリケーション全体で単一のインスタンスにする

@Singleton
class TaskDataHandlerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,

    // TODO: TaskRepository を注入してDB操作を行う

) : TaskDataHandler {
    private val TAG = "TaskDataHandlerImpl"

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun handleTaskData(data: Map<String, String>) {
        Log.d(TAG, "Handling FCM data: $data")

        val processId = data["process_id"]
        val status = data["status"]
        val title = data["messageTitle"]
        val body = data["messageBody"]

        // TODO: 受信したprocessIdとstatusを使用してアプリ内部の状態を更新する

        Log.i(TAG, "FCM Data processed: ID=$processId, Status=$status, Title=$title")

        // 基本的にサーバー側でnullにならないように管理しておく
        if (title != null && body != null) {
            sendNotification(title, body)
        } else {
            Log.e(TAG, "title is ${if (title == null) "null" else "exist"}. body is ${if (body == null) "null" else "exist"}")
        }
    }

    private fun sendNotification(title: String, body: String) {
        val channelId = appContext.getString(R.string.ml_notification_channel_id)
        val channelName = appContext.getString(R.string.ml_notification_channel_name)

        // Android 0 (API 26) 以上では通知チャンネルの作成が必要
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about ML task status changes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIcon = R.drawable.ic_launcher_foreground

        val notificationBuilder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(notificationIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // TODO: タップ時に指定の画面をタスク詳細画面に遷移できるようにする

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        Log.d(TAG, "Notification displayed: '$title' - '$body'")
    }
}