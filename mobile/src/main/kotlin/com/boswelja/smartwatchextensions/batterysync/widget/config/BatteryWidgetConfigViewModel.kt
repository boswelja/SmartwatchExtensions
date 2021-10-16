package com.boswelja.smartwatchextensions.batterysync.widget.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchDbRepository
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.devicemanagement.database.RegisteredWatchDatabaseLoader
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.wearos.discovery.WearOSDiscoveryPlatform
import kotlinx.coroutines.Dispatchers

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
            RegisteredWatchDatabaseLoader(application).createDatabase(),
            Dispatchers.IO
        )
    )

    val registeredWatches = watchRepository.registeredWatches
}
