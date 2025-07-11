package com.example.ml_notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.ml_notify.domain.TaskDataHandler
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_fcm_prefs")

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
        private val TAG = "MyFirebaseMsgService"
    }

    // HiltにTaskDataHandlerのインスタンスを注入する
    @Inject
    lateinit var taskDataHandler: TaskDataHandler

    // 子コルーチンと分離させる
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        CoroutineScope(Dispatchers.IO).launch {
            sendRegistrationToServer(token)
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

    // サーバー(Cloud Function)にトークンを送信する
    private suspend fun sendRegistrationToServer(newToken: String) {
        Log.d(TAG, "sendRegistrationTokenToServer($newToken)")

         val savedToken = dataStore.data.map { preferences ->
             preferences[FCM_TOKEN_KEY]
         }.first()

        if (savedToken == newToken) {
            Log.d(TAG, "FCM token is unchanged, skipping save.")
            return
        }

        val deviceData = hashMapOf("deviceToken" to newToken)

        // TODO: 認証機能が実装されるまではダミーユーザーを使用
        Firebase.firestore.collection("users/dummy-user/devices")
            .add(deviceData)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token successfully sent to server: $newToken")

                CoroutineScope(Dispatchers.IO).launch {
                    saveTokenToDataStore(newToken)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error sending FCM token to server", e)
            }
    }

    private suspend fun saveTokenToDataStore(token: String) {
        Log.d(TAG, "Saving FCM token to DataStore: $token")

        dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
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