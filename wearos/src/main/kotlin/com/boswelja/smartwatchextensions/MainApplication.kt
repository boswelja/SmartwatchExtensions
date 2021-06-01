package com.boswelja.smartwatchextensions

import android.app.Application
import com.boswelja.smartwatchextensions.appmanager.AppManagerWorker
import com.boswelja.smartwatchextensions.appmanager.sendAllApps
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        AppManagerWorker.enqueueWorker(this)
        GlobalScope.launch {
            sendAllApps(phoneStateStore.data.map { it.id }.first())
        }
    }
}
