package com.boswelja.smartwatchextensions

import android.app.Application
import com.boswelja.smartwatchextensions.appmanager.appManagerModule
import com.boswelja.smartwatchextensions.messages.messagesModule
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application(), DIAware {

    override val di: DI by DI.lazy {
        import(androidXModule(this@MainApplication))
        importAll(
            appManagerModule,
            messagesModule
        )
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
