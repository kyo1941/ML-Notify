package com.example.ml_notify.di

import com.example.ml_notify.data.TaskDataHandlerImpl
import com.example.ml_notify.domain.TaskDataHandler
import com.example.ml_notify.data.db.AppDatabase
import com.example.ml_notify.data.db.TaskDao
import com.example.ml_notify.data.repository.TaskRepositoryImpl
import com.example.ml_notify.domain.repository.TaskRepository
import androidx.room.Room
import android.content.Context
import com.example.ml_notify.data.provider.DeviceIdProviderImpl
import com.example.ml_notify.data.repository.DeviceRepositoryImpl
import com.example.ml_notify.domain.provider.DeviceIdProvider
import com.example.ml_notify.domain.repository.DeviceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindTaskDataHandler(taskDataHandlerImpl: TaskDataHandlerImpl): TaskDataHandler

    @Binds
    @Singleton
    abstract fun bindTaskRepository(taskRepositoryImpl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindDeviceIdProvider(deviceIdProviderImpl: DeviceIdProviderImpl): DeviceIdProvider

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(deviceRepositoryImpl: DeviceRepositoryImpl): DeviceRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // HiltでAppDatabaseのインスタンスを生成する
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "app_database"
        )
            // TODO: アプリが完成したらこっちに移行して拡張性をもたせる
            //.addMigrations()
            .fallbackToDestructiveMigration()
            .build()
    }

    // HiltでTaskDaoのインスタンスを管理できるようにする
    @Provides
    @Singleton
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }
}