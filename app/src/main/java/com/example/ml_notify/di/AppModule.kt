package com.example.ml_notify.di

import com.example.ml_notify.data.TaskDataHandlerImpl
import com.example.ml_notify.domain.TaskDataHandler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    // TaskDataHandler型にはTaskDataHandlerImplのインスタンスを注入する
    @Binds
    @Singleton
    abstract fun bindTaskDataHandler(taskDataHandlerImpl: TaskDataHandlerImpl): TaskDataHandler
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    
}