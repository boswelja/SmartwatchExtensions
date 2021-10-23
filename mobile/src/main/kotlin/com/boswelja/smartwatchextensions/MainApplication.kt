package com.boswelja.smartwatchextensions

import android.app.Application
import android.content.Context
import com.boswelja.smartwatchextensions.aboutapp.ui.aboutAppModule
import com.boswelja.smartwatchextensions.analytics.FirebaseAnalytics
import com.boswelja.smartwatchextensions.analytics.LoggingAnalytics
import com.boswelja.smartwatchextensions.appmanager.CacheValidationSerializer
import com.boswelja.smartwatchextensions.appmanager.appManagerModule
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsSerializer
import com.boswelja.smartwatchextensions.batterysync.batterySyncModule
import com.boswelja.smartwatchextensions.batterysync.batterySyncUiModule
import com.boswelja.smartwatchextensions.dashboard.dashboardModule
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.devicemanagement.deviceManagementModule
import com.boswelja.smartwatchextensions.dndsync.DnDStatusSerializer
import com.boswelja.smartwatchextensions.dndsync.dndSyncModule
import com.boswelja.smartwatchextensions.main.mainModule
import com.boswelja.smartwatchextensions.managespace.manageSpaceModule
import com.boswelja.smartwatchextensions.messages.messagesModule
import com.boswelja.smartwatchextensions.messages.messagesUiModule
import com.boswelja.smartwatchextensions.onboarding.onboardingModule
import com.boswelja.smartwatchextensions.phonelocking.phoneLockingModule
import com.boswelja.smartwatchextensions.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.settings.appSettingsModule
import com.boswelja.smartwatchextensions.settings.settingsModule
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.wearos.discovery.WearOSDiscoveryPlatform
import com.boswelja.watchconnection.wearos.message.WearOSMessagePlatform
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

@Suppress("unused")
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@MainApplication)

            modules(
                appManagerModule,
                batterySyncModule,
                deviceManagementModule,
                messagesModule,
                settingsModule,
                clientsModule,
                analyticsModule,
                watchManagerModule
            )

            modules(
                aboutAppModule,
                batterySyncUiModule,
                dashboardModule,
                com.boswelja.smartwatchextensions.devicemanagement.watchManagerModule,
                dndSyncModule,
                mainModule,
                manageSpaceModule,
                messagesUiModule,
                onboardingModule,
                phoneLockingModule,
                appSettingsModule
            )
        }
    }
}

val analyticsModule = module {
    single {
        if (BuildConfig.DEBUG) {
            LoggingAnalytics()
        } else {
            FirebaseAnalytics()
        }
    }
}

val watchManagerModule = module {
    single {
        WatchManager(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}

val clientsModule = module {
    single {
        MessageClient(
            serializers = listOf(
                IntSettingSerializer,
                BoolSettingSerializer,
                DnDStatusSerializer,
                CacheValidationSerializer,
                BatteryStatsSerializer
            ),
            platforms = listOf(
                WearOSMessagePlatform(get<Context>())
            )
        )
    }
    single {
        DiscoveryClient(
            platforms = listOf(
                WearOSDiscoveryPlatform(get())
            )
        )
    }
}
