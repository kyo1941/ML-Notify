package com.example.ml_notify.domain.repository

interface FcmTokenRepository {
    suspend fun sendRegistrationToken(newToken: String)
}