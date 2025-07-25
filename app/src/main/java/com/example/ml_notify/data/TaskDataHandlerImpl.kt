package com.example.ml_notify.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ml_notify.R
import com.example.ml_notify.domain.TaskDataHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.example.ml_notify.domain.repository.TaskRepository
import com.example.ml_notify.model.TaskStatus
import com.example.ml_notify.navigation.AppRoutes
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

@Singleton
class TaskDataHandlerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val taskRepository: TaskRepository
) : TaskDataHandler {
    private val TAG = "TaskDataHandlerImpl"

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 同時実行制御のためのprocessIdごとのMutex
    private val taskMutexes = ConcurrentHashMap<String, Mutex>()

    override suspend fun handleTaskData(data: Map<String, String>) {
        Log.d(TAG, "Handling FCM data: $data")

        val processId = data["processId"]
        val status = data["status"]

        // 開始or終了時刻データを取得する
        val startTime = data["taskActualStartTime"]?.toLongOrNull()
        val finishTime = data["taskActualCompletionTime"]?.toLongOrNull()

        // 必須パラメータのチェック．サーバー側で不正なデータは弾いているが、念のため
        if (processId == null || status == null || (startTime == null && finishTime == null)) {
            Log.e(TAG, "必須パラメータ不足: processId=$processId, status=$status, startTime=$startTime, finishTime=$finishTime")
            return
        }

        Log.i(TAG, "FCM Data processed: ID=$processId, Status=$status, " +
                "StartTime=${if (startTime != null) startTime else "null"}, " +
                "FinishTime=${if (finishTime != null) finishTime else "null"}")


        val taskStatus = when (status) {
            "START" -> TaskStatus.RUNNING
            "COMPLETED" -> TaskStatus.COMPLETED
            "FAILED" -> TaskStatus.FAILED
            else -> {
                // サーバー側で不明なステータスを弾いているので起こらない
                Log.e(TAG, "Unknown status: $status")
                return
            }
        }

        // processIdごとにMutexを取得する
        val mutex = taskMutexes.computeIfAbsent(processId) { Mutex() }


        // Mutexを使用して同時実行を制御する
        mutex.withLock {
            // 受け取ったデータでDBを更新
            val existingTask = taskRepository.getTaskById(processId)
            if (existingTask != null) {
                val updateTask = existingTask.copy(
                    status = taskStatus,
                    startTime = startTime ?: existingTask.startTime,
                    finishTime = finishTime ?: existingTask.finishTime
                )
                taskRepository.updateTask(updateTask)
                sendNotification(processId, existingTask.name, taskStatus)
            } else {
                // プログラム実行中にタスクが削除された時にはスキップする
                Log.e(TAG, "Not found task with processId: $processId")
            }
        }
    }

    private fun sendNotification(processId: String, taskName: String, taskStatus: TaskStatus) {
        val channelId = appContext.getString(R.string.ml_notification_channel_id)
        val channelName = appContext.getString(R.string.ml_notification_channel_name)

        // taskStatusに基づいて通知のタイトルと本文を生成
        val title = "タスク：$taskName"
        val body = when (taskStatus) {
            TaskStatus.RUNNING -> "${taskName}の実行を開始しました"
            TaskStatus.COMPLETED -> "${taskName}が完了しました"
            TaskStatus.FAILED -> "${taskName}が失敗しました"

            // handleTaskDataで不明なステータスは弾いているので、ここには来ないはず
            else -> "${taskStatus.name}のステータスが変更されました"
        }

        // Android 0 (API 26) 以上では通知チャンネルの作成が必要
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about ML task status changes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIcon = R.drawable.ic_launcher_foreground

        // タスク詳細画面へのDeepLinkを作成する
        val taskDetailDeepLink = AppRoutes.DeepLink.buildTaskDetailUri(processId)
        val intent = Intent(Intent.ACTION_VIEW, taskDetailDeepLink).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            processId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(notificationIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        notificationManager.notify(processId.hashCode(), notificationBuilder.build())
        Log.d(TAG, "Notification displayed: '$title' - '$body'")
    }
}