package com.example.ml_notify.domain.repository

interface DeviceRepository {
    suspend fun sendRegistrationToken(newToken: String)
}