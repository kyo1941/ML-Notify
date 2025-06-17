package com.example.ml_notify.ui.task_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.ml_notify.domain.repository.TaskRepository
import com.example.ml_notify.data.db.TaskEntity

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
): ViewModel() {
    private val _task = MutableStateFlow<TaskEntity?>(null)
    val task = _task.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun fetchTask(processId: String) {
        viewModelScope.launch {
            val taskEntity = taskRepository.getTasksById(processId)
            _task.value = taskEntity
            _message.value = taskEntity?.message ?: ""
        }
    }
}