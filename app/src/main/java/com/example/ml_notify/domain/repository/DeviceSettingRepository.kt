package com.example.ml_notify.domain.repository

import kotlinx.coroutines.flow.Flow

interface DeviceSettingRepository {
    suspend fun getDeviceName(): String
    suspend fun updateDeviceName(name: String)
    val deviceNameFlow: Flow<String>
}