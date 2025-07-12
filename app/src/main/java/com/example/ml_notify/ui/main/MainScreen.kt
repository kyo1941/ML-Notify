package com.example.ml_notify.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ml_notify.navigation.AppRoutes
import com.example.ml_notify.ui.theme.button_bg_color
import com.example.ml_notify.ui.theme.button_fg_color
import com.example.ml_notify.ui.theme.border_color

@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    val showDeviceNameRegisterDialog = remember { mutableStateOf(false) }
    val showTaskRegisterDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    val deviceName = remember { mutableStateOf("") }

    val taskName = remember { mutableStateOf("") }
    val taskMessage = remember { mutableStateOf<String?>(null) }
    val deleteTaskId = remember { mutableStateOf("") }

    val tasks by mainViewModel.tasks.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.registerEvent.collect {
            showTaskRegisterDialog.value = false
            taskName.value = ""
            taskMessage.value = null
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.updateDeviceNameEvent.collect { name ->
            showDeviceNameRegisterDialog.value = false
            deviceName.value = name
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        Column(
            modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())
        ) {

            Text(
                text = "タスク一覧",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(32.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                itemsIndexed(tasks, key = { _, task -> task.processId }) { index, task ->
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(
                                onClickLabel = "タスク「${task.name}」の詳細を開く",
                                onClick = {
                                    navController.navigate("${AppRoutes.TASK_DETAIL_SCREEN}/${task.processId}")
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = task.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                            onClick = {
                                deleteTaskId.value = task.processId
                                showDeleteDialog.value = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.ml_notify.R.drawable.baseline_delete_24),
                                contentDescription = "削除",
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (index < tasks.lastIndex) {
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
                Spacer(Modifier.width(24.dp))
                FloatingActionButton(
                    modifier = Modifier
                        .width(56.dp)
                        .height(56.dp),
                    containerColor = button_bg_color,
                    contentColor = button_fg_color,
                    shape = RoundedCornerShape(24.dp),
                    onClick = {
                        showDeviceNameRegisterDialog.value = true
                    }
                ) {
                    Icon(
                        painter = painterResource(id = com.example.ml_notify.R.drawable.outline_devices_24),
                        contentDescription = "デバイス名の変更",
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                FloatingActionButton(
                    modifier = Modifier
                        .width(56.dp)
                        .height(56.dp),
                    containerColor = button_bg_color,
                    contentColor = button_fg_color,
                    shape = RoundedCornerShape(24.dp),
                    onClick = {
                        showTaskRegisterDialog.value = true
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

    if (showDeviceNameRegisterDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeviceNameRegisterDialog.value = false },
            title = {
                Text("デバイス名の変更")
            },
            text = {
                OutlinedTextField(
                    value = deviceName.value,
                    onValueChange = { deviceName.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "デバイス名",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.updateDeviceName(deviceName.value)
                    }
                ) {
                    Text("変更")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeviceNameRegisterDialog.value = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    if (showTaskRegisterDialog.value) {
        AlertDialog(
            onDismissRequest = { showTaskRegisterDialog.value = false },
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
                                style = MaterialTheme.typography.bodyMedium
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
                                text = "説明（任意）",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.registerTask(taskName.value, taskMessage.value)
                    },
                    enabled = taskName.value.isNotBlank()
                ) {
                    Text("登録")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTaskRegisterDialog.value = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = {
                Text("確認")
            },
            text = {
                Text("このタスクを削除しますか？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.deleteTask(deleteTaskId.value)
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}