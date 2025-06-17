package com.example.ml_notify.data.db

import com.example.ml_notify.model.TaskStatus
import androidx.room.TypeConverter

class Converters {

    // ordinate(Int)ではなくname(String)で保存されるようにする
    @TypeConverter
    fun fromTaskStatus(value: TaskStatus?): String? {
        return value?.name
    }

    // StringからTaskStatusを取得する
    @TypeConverter
    fun toTaskStatus(value: String?): TaskStatus? {
        return value?.let { statusName -> TaskStatus.entries.find { it.name == statusName } }
    }
}