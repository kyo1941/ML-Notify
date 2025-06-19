package com.example.ml_notify.ui.task_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ml_notify.R
import com.example.ml_notify.model.TaskStatus

@Composable
fun TaskDetailScreen (
    navController: NavHostController,
    processId: String,
    taskDetailViewModel: TaskDetailViewModel = hiltViewModel()
) {
    val task by taskDetailViewModel.task.collectAsState()
    val message by taskDetailViewModel.message.collectAsState()

    val timeText = when {
        (task?.status == TaskStatus.COMPLETED || task?.status == TaskStatus.FAILED) && task?.startTime != null && task?.finishTime != null ->
            "開始時刻 / 終了時刻\n${taskDetailViewModel.formatTimestamp(task?.startTime)} 〜 ${taskDetailViewModel.formatTimestamp(task?.finishTime)}"
        task?.status == TaskStatus.RUNNING && task?.startTime != null ->
            "開始時刻 / 終了時刻\n${taskDetailViewModel.formatTimestamp(task?.startTime)} 〜 実行中"
        task?.status == TaskStatus.PENDING ->
            "開始時刻 / 終了時刻\n実行待ち"
        else -> "時刻情報: 取得できませんでした"
    }

    LaunchedEffect(processId) {
        taskDetailViewModel.fetchTask(processId)
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        Row {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_keyboard_arrow_left_24),
                    contentDescription = "戻る"
                )
            }

            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.weight(0.5f))

        TaskSectionScreen(header = "タスク名\n(ID: $processId)", detail = "${task?.name ?: "取得できませんでした"}")

        Spacer(Modifier.padding(vertical = 32.dp))

        TaskSectionScreen(header = "現在の状態", detail = "${task?.status ?: "取得できませんでした"}")

        Spacer(Modifier.padding(vertical = 8.dp))

        Text(
            text = timeText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.Gray
        )


        Spacer(Modifier.padding(vertical = 32.dp))

        Row {
            Spacer(Modifier.weight(0.1f))

            Text(
                text = "説明",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.padding(vertical = 16.dp))

        TextField(
            value = message ?: "",
            onValueChange = { taskDetailViewModel.updateMessage(it) },
            modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.weight(1f))
    }
}

// 各項目の出力用関数
@Composable
fun TaskSectionScreen(
    header: String,
    detail: String
) {
    Column {
        Row {
            Spacer(Modifier.weight(0.1f))
            Text(
                text = header,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.padding(vertical = 16.dp))
        Text(
            text = detail,
            Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}