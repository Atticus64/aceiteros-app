package com.example.aceiterosready

import android.app.Application
import com.example.aceiterosready.di.AppContainer

class AceiterosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
