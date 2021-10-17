package com.boswelja.smartwatchextensions.batterysync.quicksettings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsDbRepositoryLoader
import com.boswelja.smartwatchextensions.batterysync.common.getBatteryDrawableRes
import com.boswelja.smartwatchextensions.common.WatchTileService
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.main.ui.MainActivity.Companion.EXTRA_WATCH_ID
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.first
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class WatchBatteryTileService : WatchTileService(), DIAware {

    override val di: DI by closestDI()

    private val settingsRepository: WatchSettingsRepository by instance()

    override fun onClick() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_WATCH_ID, watchId)
        }
        if (isLocked) {
            unlockAndRun {
                startActivityAndCollapse(intent)
            }
        } else {
            startActivityAndCollapse(intent)
        }
    }

    override suspend fun onTileUpdateRequest(watch: Watch?) {
        if (watch == null) {
            // Couldn't get watch, set an error
            updateTile {
                label = getString(R.string.widget_watch_battery_title)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    subtitle = getString(R.string.watch_status_error)
                }
                state = Tile.STATE_UNAVAILABLE
                icon = Icon.createWithResource(
                    this@WatchBatteryTileService, R.drawable.battery_unknown
                )
            }
            return
        }

        val isBatterySyncEnabled = settingsRepository
            .getBoolean(watch.uid, BATTERY_SYNC_ENABLED_KEY, false).first()

        if (isBatterySyncEnabled) {
            val batteryStats = BatteryStatsDbRepositoryLoader
                .getInstance(this@WatchBatteryTileService)
                .batteryStatsFor(watch.uid)
                .first()

            updateTile {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    label = getString(R.string.battery_percent, batteryStats.percent.toString())
                    subtitle = watch.name
                } else {
                    label = getString(
                        R.string.battery_percent_qs_tile_fallback,
                        batteryStats.percent.toString(),
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
                    subtitle = getString(R.string.battery_sync_disabled)
                } else {
                    label = getString(R.string.battery_sync_disabled)
                }
                state = Tile.STATE_INACTIVE
                icon = Icon.createWithResource(
                    this@WatchBatteryTileService, R.drawable.battery_unknown
                )
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
