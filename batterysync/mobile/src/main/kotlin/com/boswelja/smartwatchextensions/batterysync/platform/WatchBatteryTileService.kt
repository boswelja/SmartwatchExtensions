package com.boswelja.smartwatchextensions.batterysync.platform

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryStats
import com.boswelja.smartwatchextensions.batterysync.getBatteryDrawableRes
import com.boswelja.smartwatchextensions.core.FeatureData
import com.boswelja.smartwatchextensions.core.FlowTileService
import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchRepository
import com.boswelja.smartwatchextensions.core.settings.appSettingsStore
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

/**
 * A [FlowTileService] to display battery stats for a selected watch.
 */
class WatchBatteryTileService : FlowTileService<FeatureData<BatteryStats>>() {

    private val getBatteryStats: GetBatteryStats by inject()
    private val registeredWatchRepository: RegisteredWatchRepository by inject()
    private lateinit var watch: Watch

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDataFlow(): Flow<FeatureData<BatteryStats>> {
        return appSettingsStore.data
            .map {
                if (it.qsTileWatchId.isNotBlank()) {
                    registeredWatchRepository.getWatchById(it.qsTileWatchId).firstOrNull()
                } else {
                    val watch = registeredWatchRepository
                        .registeredWatches
                        .first()
                        .firstOrNull()

                    if (watch != null) {
                        appSettingsStore.updateData { it.copy(qsTileWatchId = watch.uid) }
                    }
                    watch
                }
            }
            .filterNotNull()
            .onEach { watch = it }
            .flatMapLatest {
                getBatteryStats(it.uid)
            }
    }

    override fun onTileUpdateRequested(data: FeatureData<BatteryStats>) {
        when (data) {
            is FeatureData.Success -> {
                updateTile {
                    label = getString(
                        R.string.battery_percent,
                        data.data.percent.toString()
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subtitle = watch.name
                    }
                    state = Tile.STATE_ACTIVE
                    icon = Icon.createWithResource(
                        this@WatchBatteryTileService,
                        getBatteryDrawableRes(data.data.percent)
                    )
                }
            }
            is FeatureData.Disabled -> {
                updateTile {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subtitle = getString(R.string.qs_tile_title)
                    }
                    label = getString(R.string.battery_sync_disabled)
                    state = Tile.STATE_INACTIVE
                    icon = Icon.createWithResource(
                        this@WatchBatteryTileService,
                        com.boswelja.smartwatchextensions.batterysync.common.R.drawable.battery_unknown
                    )
                }
            }
            is FeatureData.Error -> {
                updateTile {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subtitle = getString(R.string.qs_tile_title)
                    }
                    label = getString(R.string.qs_tile_unknown_error)
                    state = Tile.STATE_UNAVAILABLE
                    icon = Icon.createWithResource(
                        this@WatchBatteryTileService,
                        com.boswelja.smartwatchextensions.batterysync.common.R.drawable.battery_unknown
                    )
                }
            }
        }
    }

    override fun onClick() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (isLocked) {
            unlockAndRun {
                startActivityAndCollapse(intent)
            }
        } else {
            startActivityAndCollapse(intent)
        }
    }

    companion object {
        /**
         * Request this QS Tile updates it's data.
         */
        fun requestTileUpdate(context: Context) {
            requestListeningState(
                context, ComponentName(context, WatchBatteryTileService::class.java)
            )
        }
    }
}
