package com.example.ml_notify.ui.task_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.ml_notify.domain.repository.TaskRepository
import com.example.ml_notify.data.db.TaskEntity
import kotlinx.coroutines.flow.debounce
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
): ViewModel() {
    private val _task = MutableStateFlow<TaskEntity?>(null)
    val task = _task.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        viewModelScope.launch {
            _message
                .debounce(500)
                .collect {
                    saveTask()
                }
        }
    }

    fun fetchTask(processId: String) {
        viewModelScope.launch {
            try {
                val taskEntity = taskRepository.getTaskById(processId)
                _task.value = taskEntity
                _message.value = taskEntity?.message ?: ""
            } catch (e: Exception) {
                Log.e("TaskDetailViewModel", "タスクの取得に失敗しました", e)
            }
        }
    }

    fun updateMessage(newMessage: String) {
        _message.value = newMessage.ifEmpty { null }
    }

    private fun saveTask() {
        val currentTask = _task.value ?: return
        if (_message.value == currentTask.message) return

        viewModelScope.launch {
            try {
                val updatedTask = currentTask.copy(message = _message.value)
                taskRepository.updateTask(updatedTask)
                _task.value = updatedTask
            } catch (e: Exception) {
                Log.e("TaskDetailViewModel", "タスクの保存に失敗しました", e)
            }
        }
    }

    fun formatTimestamp(timestamp: Long?): String {
        // 呼び出し元でのnullチェックを期待していますが、安全策としてここでもチェックする
        if (timestamp == null) return "不明"

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}