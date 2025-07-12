package com.example.ml_notify.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.UUID
import com.example.ml_notify.data.db.TaskEntity
import com.example.ml_notify.domain.repository.DeviceSettingRepository
import com.example.ml_notify.domain.repository.TaskRepository
import com.example.ml_notify.model.TaskStatus

@HiltViewModel
class MainViewModel @Inject constructor (
    private val taskRepository: TaskRepository,
    private val deviceSettingRepository: DeviceSettingRepository
) : ViewModel() {
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private val _registerEvent = MutableSharedFlow<Unit>()
    val registerEvent = _registerEvent.asSharedFlow()

    private val _taskDetailEvent = MutableSharedFlow<String>()
    val taskDetailEvent = _taskDetailEvent.asSharedFlow()

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _deviceName = MutableStateFlow("")
    val deviceName = _deviceName.asStateFlow()

    private val _updateDeviceNameEvent = MutableSharedFlow<String>()
    val updateDeviceNameEvent = _updateDeviceNameEvent.asSharedFlow()

    init {
        fetchTasks()
        fetchDeviceName()
    }

    private fun fetchTasks() {
        viewModelScope.launch {
            try {
                val taskList = taskRepository.getAllTasks()
                _tasks.value = taskList
            } catch (e: Exception) {
                Log.e("MainViewModel", "タスクの取得に失敗しました", e)
                showSnackbar("タスクの取得に失敗しました")
            }
        }
    }

    private fun fetchDeviceName() {
        viewModelScope.launch {
            try {
                val name = deviceSettingRepository.getDeviceName()
                _deviceName.value = name
            } catch (e: Exception) {
                Log.e("MainViewModel", "デバイス名の取得に失敗しました", e)
                showSnackbar("デバイス名の取得に失敗しました")
            }
        }
    }

    suspend fun showSnackbar(message: String) {
        _snackbarEvent.emit(message)
    }

    fun registerTask(taskName: String, taskMessage: String?) {
        viewModelScope.launch {
            try {
                val newTask = TaskEntity(
                    processId = UUID.randomUUID().toString(),
                    name = taskName,
                    status = TaskStatus.PENDING,
                    registeredAt = System.currentTimeMillis(),
                    startTime = null,
                    finishTime = null,
                    message = taskMessage
                )
                taskRepository.insertTask(newTask)
                fetchTasks()
                showSnackbar("タスク $taskName が登録されました")
                _registerEvent.emit(Unit)
            } catch (e: Exception) {
                Log.e("MainViewModel", "タスクの登録に失敗しました", e)
                showSnackbar("タスクの登録に失敗しました")
            }

        }
    }

    fun deleteTask(processId: String) {
        val taskName = _tasks.value.find { it.processId == processId }?.name
        val message =  taskName?.let { "タスク $taskName が削除されました" } ?: "タスクが削除されました"
        viewModelScope.launch {
            try {
                taskRepository.deleteTaskById(processId)
                fetchTasks()
                showSnackbar(message)
                _taskDetailEvent.emit(processId)
            } catch (e: Exception) {
                Log.e("MainViewModel", "タスクの削除に失敗しました", e)
                showSnackbar("タスクの削除に失敗しました")
            }
        }
    }

    fun updateDeviceName(newName: String) {
        viewModelScope.launch {
            try {
                deviceSettingRepository.updateDeviceName(newName)
                _deviceName.value = newName
                showSnackbar("デバイス名が更新されました")
                _updateDeviceNameEvent.emit(newName)
            } catch (e: Exception) {
                Log.e("MainViewModel", "デバイス名の更新に失敗しました", e)
                showSnackbar("デバイス名の更新に失敗しました")
            }
        }
    }
}