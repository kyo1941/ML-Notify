package com.example.ml_notify.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ml_notify.navigation.AppRoutes
import com.example.ml_notify.ui.theme.button_bg_color
import com.example.ml_notify.ui.theme.button_fg_color
import com.example.ml_notify.ui.theme.border_color

@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val showDialog = remember { mutableStateOf(false) }
    val taskName = remember { mutableStateOf("") }
    val taskMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        mainViewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

            Text(
                text = "タスク一覧",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(32.dp)
            )

            // TODO: DBから取得したリストに置き換えること
            val dummyList = List(5) { index -> "Item No. ${index + 1}" }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                itemsIndexed(dummyList) { index, item ->
                    Row(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = item
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            modifier = Modifier
                                .width(24.dp)
                                .height(24.dp),
                            onClick = {
                                // TODO: 現在はindexを渡しているが，正式なprocessIdを引数に渡す
                                navController.navigate("${AppRoutes.TASK_DETAIL_SCREEN}/${index + 1}")
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.ml_notify.R.drawable.baseline_keyboard_arrow_right_24),
                                contentDescription = "詳細",
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (index < dummyList.lastIndex) {
                        HorizontalDivider(
                            color = border_color,
                            thickness = 1.dp
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                Spacer(Modifier.weight(1f))
                FloatingActionButton(
                    modifier = Modifier
                        .width(56.dp)
                        .height(56.dp),
                    containerColor = button_bg_color,
                    contentColor = button_fg_color,
                    shape = RoundedCornerShape(24.dp),
                    onClick = {
                        showDialog.value = true
                    }
                ) {
                    Icon(
                        painter = painterResource(id = com.example.ml_notify.R.drawable.baseline_add_24),
                        contentDescription = "登録",
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                    )
                }
                Spacer(Modifier.width(24.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text("タスクの登録")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = taskName.value,
                        onValueChange = { taskName.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = "タスク名",
                                fontSize = 12.sp
                            )
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        // nullのときは空文字列で表示しておいて，その時はnullとして渡せるようにする
                        value = taskMessage.value ?: "",
                        onValueChange = { taskMessage.value = it.ifEmpty { null } },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = "説明",
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mainViewModel.registerTask(taskName.value, taskMessage.value)
                    showDialog.value = false
                    taskName.value = ""
                }) {
                    Text("登録")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}