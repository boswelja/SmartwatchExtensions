package com.boswelja.smartwatchextensions

import android.app.Application
import com.boswelja.smartwatchextensions.appmanager.appManagerModule
import org.kodein.di.DI
import org.kodein.di.DIAware
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application(), DIAware {

    override val di: DI by DI.lazy {
        importAll(
            appManagerModule
        )
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
