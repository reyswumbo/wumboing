package com.wumboing.app

import android.app.Application
import com.wumboing.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WumboingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WumboingApp)
            modules(appModule)
        }
    }
}
