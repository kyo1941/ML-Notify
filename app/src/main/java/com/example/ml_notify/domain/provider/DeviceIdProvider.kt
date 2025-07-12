package com.example.ml_notify.domain.provider

interface DeviceIdProvider {
    suspend fun getDeviceId(): String
    suspend fun createAndSaveDeviceId(): String
}