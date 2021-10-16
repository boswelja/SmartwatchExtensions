package com.boswelja.smartwatchextensions.dashboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepositoryLoader
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val batteryStatsRepository: BatteryStatsRepository
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val appRepository: WatchAppRepository by instance()

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        BatteryStatsRepositoryLoader.getInstance(application)
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
            appRepository.countFor(watch.uid)
        } ?: flow { emit(0L) }
    }
}
