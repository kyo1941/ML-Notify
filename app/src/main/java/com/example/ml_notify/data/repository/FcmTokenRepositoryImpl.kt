package com.example.ml_notify.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ml_notify.domain.repository.FcmTokenRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_fcm_prefs")

@Singleton
class FcmTokenRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FcmTokenRepository {

    companion object {
        private const val TAG = "FcmTokenRepository"
        val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
    }

    override suspend fun sendRegistrationToken(newToken: String) {
        Log.d(TAG, "sendRegistrationToken($newToken)")

        val savedToken = context.dataStore.data.map { preferences ->
            preferences[FCM_TOKEN_KEY]
        }.first()

        if (savedToken == newToken) {
            Log.d(TAG, "FCM token is unchanged, skipping save.")
            return
        }

        val deviceData = hashMapOf("deviceToken" to newToken)

        // TODO: 認証機能が実装されるまではダミーユーザーを使用
        try {
            Firebase.firestore.collection("users/dummy-user/devices")
                .document("device-info")
                .set(deviceData)
                .await()

            Log.d(TAG, "FCM token successfully sent to server: $newToken")

            saveTokenToDataStore(newToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending FCM token to server", e)
        }

    }

    private suspend fun saveTokenToDataStore(token: String) {
        Log.d(TAG, "Saving FCM token to DataStore: $token")

        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[FCM_TOKEN_KEY] = token
            }
        }
    }
}