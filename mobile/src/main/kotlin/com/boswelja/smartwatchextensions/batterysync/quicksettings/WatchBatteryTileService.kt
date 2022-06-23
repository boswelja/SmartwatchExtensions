package com.boswelja.smartwatchextensions.batterysync.quicksettings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.batterysync.getBatteryDrawableRes
import com.boswelja.smartwatchextensions.common.WatchTileService
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.main.ui.MainActivity.Companion.EXTRA_WATCH_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import org.koin.android.ext.android.inject

/**
 * A [WatchTileService] to display battery stats for a selected watch.
 */
class WatchBatteryTileService : WatchTileService() {

    private val settingsRepository: WatchSettingsRepository by inject()
    private val batteryStatsRepository: BatteryStatsRepository by inject()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val batteryStats = watch
        .filterNotNull()
        .flatMapLatest {
            batteryStatsRepository.batteryStatsFor(it.uid)
        }
        .stateIn(
            coroutineScope,
            SharingStarted.Eagerly,
            null
        )

    override fun onClick() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_WATCH_ID, watch.value?.uid)
        }
        if (isLocked) {
            unlockAndRun {
                startActivityAndCollapse(intent)
            }
        } else {
            startActivityAndCollapse(intent)
        }
    }

    override suspend fun onTileUpdateRequest() {
        batteryStats.collect { batteryStats ->
            val watch = watch.value
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
                return@collect
            }

            val isBatterySyncEnabled = settingsRepository
                .getBoolean(watch.uid, BATTERY_SYNC_ENABLED_KEY, false).first()

            if (isBatterySyncEnabled) {
                updateTile {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        label = getString(
                            com.boswelja.smartwatchextensions.batterysync.R.string.battery_percent,
                            batteryStats!!.percent.toString()
                        )
                        subtitle = watch.name
                    } else {
                        label = getString(
                            R.string.battery_percent_qs_tile_fallback,
                            batteryStats!!.percent.toString(),
                            watch.name
                        )
                    }
                    state = Tile.STATE_ACTIVE
                    icon = Icon.createWithResource(
                        this@WatchBatteryTileService, getBatteryDrawableRes(batteryStats.percent)
                    )
                }
            } else {
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
