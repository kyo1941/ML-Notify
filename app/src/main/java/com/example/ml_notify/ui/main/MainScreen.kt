package com.example.ml_notify.ui.main

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ml_notify.ui.theme.button_bg_color
import com.example.ml_notify.ui.theme.button_fg_color
import com.example.ml_notify.ui.theme.border_color

@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = viewModel()
) {
    Column {

        Spacer(Modifier.height(24.dp))

        // TODO: DBから取得したリストに置き換えること
        val dummyList = List(5) { index -> "Item No. ${index + 1}" }

        LazyColumn (
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            itemsIndexed(dummyList) { index, item ->
                Row (
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = item)
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        onClick = {
                            navController.navigate("taskDetail")
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

        Row (
            Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.weight(1f))
            FloatingActionButton (
                modifier = Modifier
                    .width(56.dp)
                    .height(56.dp),
                containerColor = button_bg_color,
                contentColor = button_fg_color,
                shape = RoundedCornerShape(24.dp),
                onClick = {

                // TODO: タスク登録処理に置き換えること
                Log.d("MainScreen", "Button clicked")

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