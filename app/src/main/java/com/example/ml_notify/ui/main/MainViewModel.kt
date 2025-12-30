package com.example.ml_notify.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ml_notify.data.db.TaskEntity
import com.example.ml_notify.domain.repository.DeviceRepository
import com.example.ml_notify.domain.repository.TaskRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private val _registerEvent = MutableSharedFlow<Unit>()
    val registerEvent = _registerEvent.asSharedFlow()

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks = _tasks.asStateFlow()

    init {
        initialize()
        fetchTasks()
        viewModelScope.launch {
            taskRepository.deleteOldFinishedTasks(System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000) // 3日
        }
    }

    private fun initialize() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "FCMトークンの取得に失敗しました", task.exception)
                return@addOnCompleteListener
            }

            // トークンを取得して送信
            val token = task.result
            sendRegistrationToken(token)
        }
    }

    private fun sendRegistrationToken(token: String) {
        viewModelScope.launch {
            try {
                deviceRepository.sendRegistrationToken(token)
                Log.i(TAG, "FCM Device Token sent: $token")
            } catch (e: Exception) {
                Log.e(TAG, "トークンの送信に失敗しました", e)
                showSnackbar("トークンの送信に失敗しました")
            }
        }
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

    suspend fun showSnackbar(message: String) {
        _snackbarEvent.emit(message)
    }

    fun deleteTask(processId: String) {
        val taskName = _tasks.value.find { it.id == processId }?.name
        val message = taskName?.let { "タスク $taskName が削除されました" } ?: "タスクが削除されました"
        viewModelScope.launch {
            try {
                taskRepository.deleteTaskById(processId)
                fetchTasks()
                showSnackbar(message)
            } catch (e: Exception) {
                Log.e("MainViewModel", "タスクの削除に失敗しました", e)
                showSnackbar("タスクの削除に失敗しました")
            }
        }
    }

    companion object {
        private val TAG = "MainViewModel"
    }
}