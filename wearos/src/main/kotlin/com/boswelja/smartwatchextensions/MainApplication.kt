package com.boswelja.smartwatchextensions

import android.app.Application
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
