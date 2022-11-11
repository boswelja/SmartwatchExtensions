package com.boswelja.smartwatchextensions

import android.app.Application
import com.boswelja.smartwatchextensions.batterysync.di.batterySyncModule
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.core.devicemanagement.deviceManagementModule
import com.boswelja.smartwatchextensions.dndsync.dndSyncModule
import com.boswelja.smartwatchextensions.extensions.extensionsModule
import com.boswelja.smartwatchextensions.main.ui.mainModule
import com.boswelja.smartwatchextensions.phonelocking.phoneLockingModule
import com.google.android.gms.wearable.Wearable
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
            modules(
                mainModule,
                extensionsModule
            )
            modules(
                batterySyncModule,
                deviceManagementModule,
                dndSyncModule,
                phoneLockingModule
            )
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
    single { Wearable.getMessageClient(androidContext()) }
    single { Wearable.getNodeClient(androidContext()) }
    single { Wearable.getCapabilityClient(androidContext()) }
}
