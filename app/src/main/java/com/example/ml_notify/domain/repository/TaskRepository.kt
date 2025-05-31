package com.example.ml_notify.domain.repository

import com.example.ml_notify.data.db.TaskEntity

interface TaskRepository {
    suspend fun insertTask(task: TaskEntity)
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
}