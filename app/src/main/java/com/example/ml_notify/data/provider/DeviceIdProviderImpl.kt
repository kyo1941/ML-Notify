package com.example.ml_notify.data.provider

import android.content.Context
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

/**
 *  端末固有の ID を生成・管理する。DeviceIdProvider の実装クラス
 *
 *  @param context [Context]
 */
@Singleton
class DeviceIdProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceIdProvider {
    /**  Device */
    override suspend fun getDeviceId(): String {
        return context.dataStore.data.map { preferences ->
            preferences[DEVICE_ID_KEY]
        }.first() ?: run {
            val  newDeviceId = UUID.randomUUID().toString()
            context.dataStore.edit { preferences ->
                preferences[DEVICE_ID_KEY] = newDeviceId
            }
            return newDeviceId
        }
    }

    companion object {
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }
}