package com.example.ml_notify.data.repository

import com.example.ml_notify.data.db.TaskDao
import com.example.ml_notify.data.db.TaskEntity
import com.example.ml_notify.domain.repository.TaskRepository
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
): TaskRepository {
    override suspend fun insertTask(task: TaskEntity) {
        taskDao.insert(task)
    }

    override suspend fun updateTask(task: TaskEntity) {
        taskDao.update(task)
    }

    override suspend fun deleteTaskById(processId: String) {
        taskDao.deleteTaskById(processId)
    }

    override suspend fun getTaskById(processId: String): TaskEntity? {
        return taskDao.getTaskById(processId)
    }

    override suspend fun getAllTasks(): List<TaskEntity> {
        return taskDao.getAllTasks()
    }
}