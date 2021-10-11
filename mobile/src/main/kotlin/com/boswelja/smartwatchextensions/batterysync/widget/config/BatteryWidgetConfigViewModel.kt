package com.boswelja.smartwatchextensions.batterysync.widget.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.watchmanager.WatchDbRepository
import com.boswelja.smartwatchextensions.watchmanager.WatchRepository
import com.boswelja.smartwatchextensions.watchmanager.database.RegisteredWatchDatabaseLoader
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.wearos.discovery.WearOSDiscoveryPlatform

class BatteryWidgetConfigViewModel internal constructor(
    application: Application,
    watchRepository: WatchRepository
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchDbRepository(
            DiscoveryClient(
                listOf(
                    WearOSDiscoveryPlatform(application)
                )
            ),
            RegisteredWatchDatabaseLoader(application).createDatabase()
        )
    )

    val registeredWatches = watchRepository.registeredWatches
}
