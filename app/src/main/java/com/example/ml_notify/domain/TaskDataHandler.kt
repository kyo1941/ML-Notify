package com.example.ml_notify.domain

interface TaskDataHandler {
    suspend fun handleTaskData(data: Map<String, String>)
}