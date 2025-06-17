package com.example.ml_notify.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.UUID
import com.example.ml_notify.data.db.TaskEntity
import com.example.ml_notify.domain.repository.TaskRepository
import com.example.ml_notify.model.TaskStatus

@HiltViewModel
class MainViewModel @Inject constructor (
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    suspend fun showSnackbar(message: String) {
        _snackbarEvent.emit(message)
    }

    fun registerTask(taskName: String, taskMessage: String?) {
        viewModelScope.launch {
            val newTask = TaskEntity(
                processId = UUID.randomUUID().toString(),
                name = taskName,
                status = TaskStatus.PENDING,
                registeredAt = System.currentTimeMillis(),
                startTime = null,
                message = taskMessage
            )

            taskRepository.insertTask(newTask)

            showSnackbar("タスク $taskName が登録されました")
        }
    }
}