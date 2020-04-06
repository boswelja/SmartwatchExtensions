/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.batterysync

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.ui.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionInterface
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class BatterySyncPreferenceWidgetFragment :
        Fragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        WatchConnectionInterface {

    private val coroutineScope = MainScope()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var activity: BatterySyncPreferenceActivity

    private lateinit var watchBatteryIndicator: AppCompatImageView
    private lateinit var watchBatteryPercent: AppCompatTextView
    private lateinit var watchBatteryLastUpdated: AppCompatTextView

    private var watchBatteryUpdateTimer: Timer? = null
    private var watchBatteryUpdateTimerStarted: Boolean = false

    private var batterySyncEnabled: Boolean = false
    private var batteryStats: WatchBatteryStats? = null

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BATTERY_SYNC_ENABLED_KEY -> {
                batterySyncEnabled = sharedPreferences?.getBoolean(BATTERY_SYNC_ENABLED_KEY, false) == true
                if (batterySyncEnabled) {
                    startBatteryUpdateTimer()
                } else {
                    updateWatchBatteryPercent()
                    updateBatterySyncLastTimeNow()
                    stopBatteryUpdateTimer()
                }
            }
        }
    }

    override fun onWatchAdded(watch: Watch) {} // Do nothing

    override fun onConnectedWatchChanging() {} // Do nothing

    override fun onConnectedWatchChanged(success: Boolean) {
        if (success and batterySyncEnabled) {
            updateBatteryStats(context!!, activity.watchConnectionManager?.getConnectedWatchId())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)

        activity = getActivity() as BatterySyncPreferenceActivity
        activity.batteryStatsDatabaseEventInterface = object : BatterySyncPreferenceActivity.BatteryStatsDatabaseEventInterface {
            override fun onOpened() {
                coroutineScope.launch {
                    updateBatteryStatsDisplay()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_widget_battery_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        watchBatteryIndicator = view.findViewById(R.id.watch_battery_indicator)
        watchBatteryPercent = view.findViewById(R.id.watch_battery_percent)
        watchBatteryLastUpdated = view.findViewById(R.id.last_updated_time)
    }

    override fun onResume() {
        super.onResume()

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        startBatteryUpdateTimer()
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        stopBatteryUpdateTimer()
    }

    private fun updateWatchBatteryPercent() {
        if (batterySyncEnabled) {
            if (batteryStats != null && batteryStats!!.batteryPercent > 0) {
                watchBatteryIndicator.setImageLevel(batteryStats!!.batteryPercent)
                watchBatteryPercent.text = getString(R.string.battery_sync_percent_short, batteryStats!!.batteryPercent.toString())
            } else {
                watchBatteryPercent.text = "Error"
            }
        } else {
            watchBatteryIndicator.setImageLevel(0)
            watchBatteryPercent.text = getString(R.string.battery_sync_disabled)
        }
    }

    private fun updateBatterySyncLastTimeNow() {
        if (batterySyncEnabled) {
            if (batteryStats != null && batteryStats!!.batteryPercent > 0) {
                val lastUpdatedMillis = System.currentTimeMillis() - batteryStats!!.lastUpdatedMillis
                val lastUpdatedMinutes = TimeUnit.MILLISECONDS.toMinutes(lastUpdatedMillis).toInt()
                val lastUpdatedString = when {
                    lastUpdatedMinutes < 1 -> {
                        getString(R.string.battery_sync_last_updated_under_minute)
                    }
                    lastUpdatedMinutes < 60 -> {
                        resources.getQuantityString(R.plurals.battery_sync_last_updated_minutes, lastUpdatedMinutes, lastUpdatedMinutes)
                    }
                    else -> {
                        val lastUpdatedHours = TimeUnit.MINUTES.toHours(lastUpdatedMinutes.toLong()).toInt()
                        resources.getQuantityString(R.plurals.battery_sync_last_updated_hours, lastUpdatedHours, lastUpdatedHours)
                    }
                }
                this@BatterySyncPreferenceWidgetFragment.activity.runOnUiThread {
                    watchBatteryLastUpdated.text = lastUpdatedString
                    watchBatteryLastUpdated.visibility = View.VISIBLE
                }
            }
        } else {
            watchBatteryLastUpdated.visibility = View.GONE
        }
    }

    private fun startBatteryUpdateTimer() {
        if (!watchBatteryUpdateTimerStarted && batterySyncEnabled) {
            watchBatteryUpdateTimer = fixedRateTimer("batterySyncLastTimeTimer", false, 0L, 60 * 1000) {
                coroutineScope.launch {
                    updateBatteryStatsDisplay()
                }
            }
            watchBatteryUpdateTimerStarted = true
        }
    }

    private fun stopBatteryUpdateTimer() {
        if (watchBatteryUpdateTimerStarted) {
            watchBatteryUpdateTimer?.cancel()
            watchBatteryUpdateTimer?.purge()
            watchBatteryUpdateTimerStarted = false
        }
    }

    private suspend fun updateBatteryStatsDisplay() {
        withContext(Dispatchers.IO) {
            batteryStats = activity.batteryStatsDatabase?.batteryStatsDao()?.getStatsForWatch(activity.watchConnectionManager?.getConnectedWatchId()!!)

            withContext(Dispatchers.Main) {
                updateWatchBatteryPercent()
                updateBatterySyncLastTimeNow()
            }
        }
    }
}
