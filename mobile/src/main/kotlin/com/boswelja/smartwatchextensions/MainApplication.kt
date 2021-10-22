package com.boswelja.smartwatchextensions

import android.app.Application
import android.content.Context
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.analytics.FirebaseAnalytics
import com.boswelja.smartwatchextensions.analytics.LoggingAnalytics
import com.boswelja.smartwatchextensions.appmanager.CacheValidationSerializer
import com.boswelja.smartwatchextensions.appmanager.appManagerModule
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.batterySyncModule
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.devicemanagement.deviceManagementModule
import com.boswelja.smartwatchextensions.dndsync.DnDStatusSerializer
import com.boswelja.smartwatchextensions.messages.messagesModule
import com.boswelja.smartwatchextensions.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.settings.settingsModule
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.wearos.discovery.WearOSDiscoveryPlatform
import com.boswelja.watchconnection.wearos.message.WearOSMessagePlatform
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application(), DIAware {

    override val di: DI by DI.lazy {
        import(androidXModule(this@MainApplication))
//        importAll(
//            appManagerModule,
//            batterySyncModule,
//            deviceManagementModule,
//            messagesModule,
//            settingsModule
//        )
        bind<MessageClient>() with singleton {
            MessageClient(
                serializers = listOf(
                    IntSettingSerializer,
                    BoolSettingSerializer,
                    DnDStatusSerializer,
                    CacheValidationSerializer,
                    BatteryStatsSerializer
                ),
                platforms = listOf(
                    WearOSMessagePlatform(instance<Context>())
                )
            )
        }
        bind<DiscoveryClient>() with singleton {
            DiscoveryClient(
                platforms = listOf(
                    WearOSDiscoveryPlatform(instance())
                )
            )
        }
        bind<Analytics>() with singleton {
            if (BuildConfig.DEBUG) {
                LoggingAnalytics()
            } else {
                FirebaseAnalytics()
            }
        }
        bind<WatchManager>() with singleton {
            WatchManager(
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
