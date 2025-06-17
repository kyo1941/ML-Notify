package com.example.ml_notify.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ml_notify.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity (
    @PrimaryKey
    val processId: String,
    val name: String,
    val status: TaskStatus,
    val registeredAt: Long,
    val startTime: Long?,
    val message: String?
)