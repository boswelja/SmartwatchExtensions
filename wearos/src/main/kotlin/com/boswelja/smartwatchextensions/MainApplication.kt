package com.boswelja.smartwatchextensions

import android.app.Application
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.main.ui.mainModule
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * A custom [Application] implementation for starting Koin.
 */
@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(clientsModule, miscModule)
            modules(mainModule)
        }
    }
}

/**
 * A Koin module for providing CapabilityUpdater.
 */
val miscModule = module {
    single { CapabilityUpdater(get(), get()) }
}

/**
 * A Koin module for providing clients.
 */
val clientsModule = module {
    single {
        MessageClient(get())
    }
    single {
        DiscoveryClient(get())
    }
}
