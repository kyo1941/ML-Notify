package com.example.ml_notify

import android.util.Log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.ml_notify.domain.TaskDataHandler
import com.example.ml_notify.domain.repository.FcmTokenRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private val TAG = "MyFirebaseMsgService"
    }

    // HiltにTaskDataHandlerのインスタンスを注入する
    @Inject
    lateinit var taskDataHandler: TaskDataHandler

    @Inject
    lateinit var fcmTokenRepository: FcmTokenRepository

    // 子コルーチンと分離させる
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        serviceScope.launch {
            fcmTokenRepository.sendRegistrationToken(token)
        }
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