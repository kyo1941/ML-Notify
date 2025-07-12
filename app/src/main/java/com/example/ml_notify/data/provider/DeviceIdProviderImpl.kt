package com.example.ml_notify.data.provider

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ml_notify.domain.provider.DeviceIdProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "device_id_prefs")

@Singleton
class DeviceIdProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceIdProvider {
    companion object {
        private const val TAG = "DeviceIdProviderImpl"
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }

    override suspend fun getDeviceId(): String {
        return context.dataStore.data.map { preferences ->
            preferences[DEVICE_ID_KEY]
        }.first() ?: run {
            createAndSaveDeviceId()
        }
    }

    override suspend fun createAndSaveDeviceId(): String {
        val newDeviceId = UUID.randomUUID().toString()
        Log.d(TAG, "Created new device ID: $newDeviceId")

        context.dataStore.edit { preferences ->
            preferences[DEVICE_ID_KEY] = newDeviceId
        }

        return newDeviceId
    }
}