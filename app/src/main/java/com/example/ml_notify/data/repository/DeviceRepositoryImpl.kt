package com.example.ml_notify.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ml_notify.domain.provider.DeviceIdProvider
import com.example.ml_notify.domain.repository.DeviceRepository
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_fcm_prefs")

/**
 * プッシュ通知用の Token を管理してサーバーに登録する。FcmTokenRepository の実装クラス
 *
 * @param context [Context]
 * @param deviceIdProvider [DeviceIdProvider]
 */
@Singleton
class DeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdProvider: DeviceIdProvider
) : DeviceRepository {
    override suspend fun sendRegistrationToken(newToken: String) {
        val savedToken = context.dataStore.data.map { preferences ->
            preferences[FCM_TOKEN_KEY]
        }.first()

        if (savedToken == newToken) return

        val deviceId = deviceIdProvider.getDeviceId()
        val deviceName =  Build.MODEL
        val deviceData = hashMapOf(
            "deviceName" to deviceName,
            "deviceToken" to newToken
        )

        // TODO: 認証機能が実装されるまではダミーユーザーを使用
        try {
            Firebase.firestore.collection("users/dummy-user/devices")
                .document(deviceId)
                .set(deviceData, SetOptions.merge())
                .await()
            context.dataStore.edit { preferences ->
                preferences[FCM_TOKEN_KEY] = newToken
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending FCM token to server", e)
        }
    }

    companion object {
        private const val TAG = "FcmTokenRepository"
        val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
    }
}