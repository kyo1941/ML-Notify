package com.example.ml_notify.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ml_notify.domain.repository.DeviceSettingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "device_settings")

@Singleton
class DeviceSettingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceSettingRepository {
    companion object {
        val DEVICE_NAME_KEY = stringPreferencesKey("device_name")
        private const val DEVICE_NAME_DEFAULT = "未設定"
    }

    override val deviceNameFlow = context.dataStore.data.map { preferences ->
        preferences[DEVICE_NAME_KEY] ?: DEVICE_NAME_DEFAULT
    }

    override suspend fun getDeviceName(): String = deviceNameFlow.first()

    override suspend fun updateDeviceName(name: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[DEVICE_NAME_KEY] = name
            }
        }
    }
}