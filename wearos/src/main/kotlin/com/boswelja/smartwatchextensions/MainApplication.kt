package com.boswelja.smartwatchextensions

import android.app.Application
import com.boswelja.smartwatchextensions.appmanager.AppListSerializer
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.devicemanagement.VersionSerializer
import com.boswelja.smartwatchextensions.dndsync.DnDStatusSerializer
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.singleton
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application(), DIAware {

    override val di: DI by DI.lazy {
        bind<MessageClient>() with singleton {
            MessageClient(
                this@MainApplication,
                listOf(
                    AppListSerializer,
                    BatteryStatsSerializer,
                    DnDStatusSerializer,
                    VersionSerializer
                )
            )
        }
        bind<DiscoveryClient>() with singleton {
            DiscoveryClient(this@MainApplication)
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
