package com.boswelja.smartwatchextensions.batterysync.quicksettings

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryStats
import com.boswelja.smartwatchextensions.batterysync.getBatteryDrawableRes
import com.boswelja.smartwatchextensions.common.WatchTileService
import com.boswelja.smartwatchextensions.core.FeatureData
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.settings.appSettingsStore
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject

/**
 * A [WatchTileService] to display battery stats for a selected watch.
 */
class WatchBatteryTileService : WatchTileService() {

    private val getBatteryStats: GetBatteryStats by inject()
    private val watchRepository: WatchRepository by inject()

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

    override suspend fun onTileUpdateRequest() {
        val watch = getWatch()
        if (watch == null) {
            // Couldn't get watch, set an error
            updateTile {
                label = getString(R.string.widget_watch_battery_title)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    subtitle = getString(R.string.watch_status_error)
                }
                state = Tile.STATE_UNAVAILABLE
                icon = Icon.createWithResource(
                    this@WatchBatteryTileService,
                    com.boswelja.smartwatchextensions.batterysync.common.R.drawable.battery_unknown
                )
            }
            return
        }

        when (val batteryStatsFeatureData = getBatteryStats(watch.uid).first()) {
            is FeatureData.Success -> {
                updateTile {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        label = getString(
                            com.boswelja.smartwatchextensions.batterysync.R.string.battery_percent,
                            batteryStatsFeatureData.data.percent.toString()
                        )
                        subtitle = watch.name
                    } else {
                        label = getString(
                            R.string.battery_percent_qs_tile_fallback,
                            batteryStatsFeatureData.data.percent.toString(),
                            watch.name
                        )
                    }
                    state = Tile.STATE_ACTIVE
                    icon = Icon.createWithResource(
                        this@WatchBatteryTileService,
                        getBatteryDrawableRes(batteryStatsFeatureData.data.percent)
                    )
                }
            }
            is FeatureData.Disabled -> {
                updateTile {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        label = getString(R.string.widget_watch_battery_title)
                        subtitle = getString(
                            com.boswelja.smartwatchextensions.batterysync.R.string.battery_sync_disabled
                        )
                    } else {
                        label = getString(com.boswelja.smartwatchextensions.batterysync.R.string.battery_sync_disabled)
                    }
                    state = Tile.STATE_INACTIVE
                    icon = Icon.createWithResource(
                        this@WatchBatteryTileService,
                        com.boswelja.smartwatchextensions.batterysync.common.R.drawable.battery_unknown
                    )
                }
            }
            is FeatureData.Error -> {
                updateTile {
                    label = getString(R.string.widget_watch_battery_title)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        subtitle = getString(R.string.watch_status_error)
                    }
                    state = Tile.STATE_UNAVAILABLE
                    icon = Icon.createWithResource(
                        this@WatchBatteryTileService,
                        com.boswelja.smartwatchextensions.batterysync.common.R.drawable.battery_unknown
                    )
                }
            }
        }
    }

    private suspend fun getWatch(): Watch? {
        return appSettingsStore.data.map {
            if (it.qsTileWatchId.isNotBlank()) {
                watchRepository.getWatchById(it.qsTileWatchId).firstOrNull()
            } else {
                val watch = watchRepository
                    .registeredWatches
                    .first()
                    .firstOrNull()

                if (watch != null) {
                    appSettingsStore.updateData { it.copy(qsTileWatchId = watch.uid) }
                }
                watch
            }
        }.first()
    }

    private fun updateTile(updates: Tile.() -> Unit) {
        qsTile.apply {
            updates()
        }.also { it.updateTile() }
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
