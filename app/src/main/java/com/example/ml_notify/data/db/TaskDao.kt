package com.example.ml_notify.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :processId")
    suspend fun deleteTaskById(processId: String)

    @Query("SELECT * FROM tasks WHERE id = :processId")
    suspend fun getTaskById(processId: String): TaskEntity?

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    /**
     * 終了タスクの削除漏れを自動で削除する
     *
     * @param threshold 終了後から削除までの猶予時間
     */
    @Query("DELETE FROM tasks WHERE finishTime IS NOT NULL AND finishTime < :threshold")
    suspend fun deleteOldFinishedTasks(threshold: Long)
}