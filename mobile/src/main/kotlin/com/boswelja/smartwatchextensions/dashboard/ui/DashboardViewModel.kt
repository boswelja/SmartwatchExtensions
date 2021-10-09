package com.boswelja.smartwatchextensions.dashboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepositoryLoader
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val batteryStatsRepository: BatteryStatsRepository,
    private val appDatabase: WatchAppDatabase
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        BatteryStatsRepositoryLoader.getInstance(application),
        WatchAppDatabase.getInstance(application)
    )

    val status = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)
        } ?: flowOf(ConnectionMode.Disconnected)
    }

    val batteryStats = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            batteryStatsRepository.batteryStatsFor(watch.uid)
        } ?: flowOf(null)
    }

    val appCount = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            appDatabase.apps().countForWatch(watch.uid)
        } ?: flow { emit(0) }
    }
}
