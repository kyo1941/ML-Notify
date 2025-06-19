package com.example.ml_notify.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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

@Singleton
class TaskDataHandlerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val taskRepository: TaskRepository
) : TaskDataHandler {
    private val TAG = "TaskDataHandlerImpl"

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun handleTaskData(data: Map<String, String>) {
        Log.d(TAG, "Handling FCM data: $data")

        val processId = data["processId"]
        val status = data["status"]
        val title = data["messageTitle"]
        val body = data["messageBody"]

        // 開始or終了時刻データを取得する
        val startTime = data["taskActualStartTime"]?.toLongOrNull()
        val finishTime = data["taskActualCompletionTime"]?.toLongOrNull()

        // 必須パラメータのチェック．サーバー側で不正なデータは弾いているが、念のため
        if (processId == null || status == null || title == null || body == null || (startTime == null && finishTime == null)) {
            Log.e(TAG, "必須パラメータ不足: processId=${if (processId == null) "null" else "exist"}, " +
                    "status=${if (status == null) "null" else "exist"}, " +
                    "title=${if (title == null) "null" else "exist"}, " +
                    "body=${if (body == null) "null" else "exist"}, " +
                    "startTime=${if (startTime == null) "null" else startTime}, " +
                    "finishTime=${if (finishTime == null) "null" else finishTime}")
            return
        }

        Log.i(TAG, "FCM Data processed: ID=$processId, Status=$status, Title=$title, Body=$body, " +
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

        // 受け取ったデータでDBを更新
        val existingTask = taskRepository.getTaskById(processId)
        if(existingTask != null) {
            val updateTask = existingTask.copy(
                status = taskStatus,
                startTime = startTime ?: existingTask.startTime,
                finishTime = finishTime ?: existingTask.finishTime
            )
            taskRepository.updateTask(updateTask)
        } else {
            // プログラム実行中にタスクが削除された時にはスキップする
            Log.e(TAG, "Not found task with processId: $processId")
            return
        }

        sendNotification(processId, title, body)
    }

    private fun sendNotification(processId: String, title: String, body: String) {
        val channelId = appContext.getString(R.string.ml_notification_channel_id)
        val channelName = appContext.getString(R.string.ml_notification_channel_name)

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

        val notificationBuilder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(notificationIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // TODO: タップ時に指定の画面をタスク詳細画面に遷移できるようにする

        notificationManager.notify(processId.hashCode(), notificationBuilder.build())
        Log.d(TAG, "Notification displayed: '$title' - '$body'")
    }
}