package com.boswelja.smartwatchextensions.dashboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.discovery.Status
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val batteryStatsDatabase: WatchBatteryStatsDatabase,
    private val appDatabase: WatchAppDatabase
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        WatchBatteryStatsDatabase.getInstance(application),
        WatchAppDatabase.getInstance(application)
    )

    val status = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)
        } ?: flow {
            emit(Status.ERROR)
        }
    }

    val batteryStats = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            batteryStatsDatabase.batteryStatsDao().getStats(watch.id)
        } ?: flow { emit(null) }
    }

    val appCount = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            appDatabase.apps().countForWatch(watch.id)
        } ?: flow { emit(0) }
    }
}
