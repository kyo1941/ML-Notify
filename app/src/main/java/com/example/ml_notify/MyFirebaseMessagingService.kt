package com.example.ml_notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.ml_notify.domain.TaskDataHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"

    // HiltにTaskDataHandlerのインスタンスを注入する
    @Inject
    lateinit var taskDataHandler: TaskDataHandler

    // 子コルーチンと分離させる
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        sendRegistrationToServer(token)
    }

    // フォアグラウンド時に実行される
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // データメッセージが受信されたか
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // 今回は即時処理が想定されるため scheduleJob() は省略
            handleNow(remoteMessage.data)
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification body: ${it.body}")
        }
    }

    // サーバー(Cloud Function)にトークンを送信する
    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")

        // TODO: サーバーにトークンを送信するロジックの実装
    }

    //
    private fun handleNow(data: Map<String, String>) {
        Log.d(TAG, "Handling message now using injected TaskDataHandler...")

        serviceScope.launch {
            try {
                taskDataHandler.handleTaskData(data)
            } catch (e: Exception) {
                Log.e(TAG, "Error in TaskDataHandler while processing FCM data", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // 破棄されるタイミングでコルーチンはキャンセル
        serviceJob.cancel()
        Log.d(TAG, "MyFirebaseMessagingService destroyed, coroutine scope cancelled.")
    }
}