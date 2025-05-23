package com.example.ml_notify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import android.util.Log

//
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("MyApplication", "Application created")
    }
}