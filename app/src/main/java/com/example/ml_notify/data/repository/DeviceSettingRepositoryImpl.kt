package com.example.ml_notify.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ml_notify.domain.provider.DeviceIdProvider
import com.example.ml_notify.domain.repository.DeviceSettingRepository
import com.google.firebase.firestore.SetOptions
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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "device_settings")

@Singleton
class DeviceSettingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdProvider: DeviceIdProvider
) : DeviceSettingRepository {
    companion object {
        private const val TAG = "DeviceSettingRepository"
        val DEVICE_NAME_KEY = stringPreferencesKey("device_name")
        private const val DEVICE_NAME_DEFAULT = "未設定"
    }

    override val deviceNameFlow = context.dataStore.data.map { preferences ->
        preferences[DEVICE_NAME_KEY] ?: DEVICE_NAME_DEFAULT
    }

    override suspend fun getDeviceName(): String = deviceNameFlow.first()

    override suspend fun sendDeviceName(newDeviceName: String) {
        Log.d(TAG, "sendDeviceName($newDeviceName)")

        val savedDeviceName = getDeviceName()

        if (savedDeviceName == newDeviceName) {
            Log.d(TAG, "Device Name is unchanged, skipping save.")
            return
        }

        val deviceNameData = hashMapOf("deviceName" to newDeviceName)

        val deviceId = deviceIdProvider.getDeviceId()

        // TODO: 認証機能が実装されるまではダミーユーザーを使用
        try {
            Firebase.firestore.collection("users/dummy-user/devices")
                .document(deviceId)
                .set(deviceNameData, SetOptions.merge())
                .await()

            Log.d(TAG, "Device Name successfully sent to server: $newDeviceName")

            saveDeviceNameToDataStore(newDeviceName)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending Device Name to server", e)
        }
    }

    private suspend fun saveDeviceNameToDataStore(name: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[DEVICE_NAME_KEY] = name
            }
        }
    }
}