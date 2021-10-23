package com.boswelja.smartwatchextensions

import android.app.Application
import com.boswelja.smartwatchextensions.appmanager.AppListSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.devicemanagement.VersionSerializer
import com.boswelja.smartwatchextensions.dndsync.DnDStatusSerializer
import com.boswelja.smartwatchextensions.main.ui.mainModule
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        startKoin {
            androidContext(this@MainApplication)
            modules(clientsModule, miscModule)
            modules(mainModule)
        }
    }
}

val miscModule = module {
    single { CapabilityUpdater(get(), get()) }
}

val clientsModule = module {
    single {
        MessageClient(
            get(),
            listOf(
                AppListSerializer,
                BatteryStatsSerializer,
                DnDStatusSerializer,
                VersionSerializer
            )
        )
    }
    single {
        DiscoveryClient(get())
    }
}
