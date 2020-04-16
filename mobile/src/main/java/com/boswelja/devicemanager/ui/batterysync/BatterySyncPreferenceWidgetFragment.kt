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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.databinding.SettingsWidgetBatterySyncBinding
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

    private lateinit var binding: SettingsWidgetBatterySyncBinding

    private var watchBatteryUpdateTimer: Timer? = null
    private var watchBatteryUpdateTimerStarted: Boolean = false

    private var batterySyncEnabled: Boolean = false
    private var batteryStats: WatchBatteryStats? = null

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BATTERY_SYNC_ENABLED_KEY -> {
                val newBatterySyncState = sharedPreferences?.getBoolean(BATTERY_SYNC_ENABLED_KEY, false) == true
                if (newBatterySyncState != batterySyncEnabled) {
                    batterySyncEnabled = newBatterySyncState
                    if (batterySyncEnabled) {
                        setWatchBatteryLoading()
                        startBatteryUpdateTimer()
                    } else {
                        updateWatchBatteryPercent()
                        updateBatterySyncLastTimeNow()
                        stopBatteryUpdateTimer()
                    }
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
        batterySyncEnabled = sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)

        activity = getActivity() as BatterySyncPreferenceActivity
        activity.batteryStatsDatabaseEventInterface = object : BatterySyncPreferenceActivity.BatteryStatsDatabaseEventInterface {
            override fun onOpened() {
                coroutineScope.launch(Dispatchers.IO) {
                    updateBatteryStatsDisplay()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.settings_widget_battery_sync, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        startBatteryUpdateTimer()
    }

    override fun onStop() {
        super.onStop()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        stopBatteryUpdateTimer()
    }

    private fun updateWatchBatteryPercent() {
        binding.apply {
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
    }

    private fun setWatchBatteryLoading() {
        binding.apply {
            watchBatteryIndicator.setImageLevel(0)
            watchBatteryPercent.text = "Please Wait"
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
                    binding.apply {
                        lastUpdatedTime.text = lastUpdatedString
                        lastUpdatedTime.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            this@BatterySyncPreferenceWidgetFragment.activity.runOnUiThread {
                binding.apply {
                    lastUpdatedTime.visibility = View.GONE
                }
            }
        }
    }

    private fun startBatteryUpdateTimer() {
        if (!watchBatteryUpdateTimerStarted && batterySyncEnabled) {
            watchBatteryUpdateTimer = fixedRateTimer("batterySyncLastTimeTimer", false, 3 * 1000, 60 * 1000) {
                coroutineScope.launch(Dispatchers.IO) {
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
