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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.example.ml_notify.R

@Composable
fun TaskDetailScreen (
    navController: NavHostController,
    processId: String
) {
    // TODO: processIdを用いてDBから情報を取得する (更新のタイミングが悪いときにも対応できるように引数全体は渡さない)
    var message by remember { mutableStateOf("hogehoge") }

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

        // TODO: 渡されたタスク情報から取得したものを配置する
        TaskSectionScreen(header = "タスク名($processId)", detail = "ここにタスク名が出力されます")

        Spacer(Modifier.padding(vertical = 32.dp))

        // TODO: 渡されたタスク情報から取得したものを配置する
        TaskSectionScreen(header = "現在の状態", detail = "ここに現在の状態が出力されます")

        Spacer(Modifier.padding(vertical = 8.dp))

        Text(
            text = "開始時刻 or 終了時刻: hogehoge",
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

        // TODO: 渡されたタスク情報から取得したものを配置する．オプショナルなので書き込んだのち保存する
        TextField(
            value = message,
            onValueChange = { message = it },
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