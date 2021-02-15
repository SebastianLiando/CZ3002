package com.guavas.cz3002.dependency

import android.app.Application
import com.guavas.cz3002.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class HiltApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.i("Timber tree planted")
        }
    }
}