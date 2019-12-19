/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_LAST_WHEN_KEY
import com.boswelja.devicemanager.ui.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionInterface
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

class BatterySyncPreferenceWidgetFragment :
        Fragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        WatchConnectionInterface {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var activity: BatterySyncPreferenceActivity

    private var watchBatteryIndicator: AppCompatImageView? = null
    private var watchBatteryPercent: AppCompatTextView? = null
    private var watchBatteryLastUpdated: AppCompatTextView? = null
    private var watchBatteryUpdateNowHolder: View? = null

    private var watchBatteryLastUpdateTimeTimer: Timer? = null

    private var batterySyncEnabled: Boolean = false
    private var batteryPercent: Int = 0
    private var lastUpdateTime: Long = 0

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BATTERY_SYNC_ENABLED_KEY -> {
                batterySyncEnabled = sharedPreferences?.getBoolean(BATTERY_SYNC_ENABLED_KEY, false) == true
                updateWatchBatteryPercent()
                updateBatterySyncLastTimeNow()
                startBatterySyncLastTimeTimer()
            }
            BATTERY_PERCENT_KEY -> {
                 batteryPercent = sharedPreferences?.getInt(BATTERY_PERCENT_KEY, 0) ?: 0
                 updateWatchBatteryPercent()
            }
            BATTERY_SYNC_LAST_WHEN_KEY -> {
                lastUpdateTime = sharedPreferences?.getLong(BATTERY_SYNC_LAST_WHEN_KEY, 0) ?: 0
                if (batterySyncEnabled) {
                    updateBatterySyncLastTimeNow()
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
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        watchBatteryLastUpdateTimeTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        batterySyncEnabled = sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false) == true
        batteryPercent = sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0)
        lastUpdateTime = sharedPreferences.getLong(BATTERY_SYNC_LAST_WHEN_KEY, 0)
        if (batterySyncEnabled) {
            updateWatchBatteryPercent()
            updateBatterySyncLastTimeNow()
            startBatterySyncLastTimeTimer()
        }

        startBatterySyncLastTimeTimer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_widget_battery_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        watchBatteryIndicator = view.findViewById(R.id.watch_battery_indicator)
        watchBatteryPercent = view.findViewById(R.id.watch_battery_percent)
        watchBatteryLastUpdated = view.findViewById(R.id.last_updated_time)

        watchBatteryUpdateNowHolder = view.findViewById<View>(R.id.updated_time_holder).apply {
            setOnClickListener {
                if (batterySyncEnabled) {
                    updateBatteryStats(context!!, activity.watchConnectionManager?.getConnectedWatchId())
                }
            }
        }
    }

    private fun updateWatchBatteryPercent() {
        if (batterySyncEnabled) {
            if (batteryPercent > 0) {
                watchBatteryIndicator?.setImageLevel(batteryPercent)
                watchBatteryPercent?.text = getString(R.string.battery_sync_percent_short, batteryPercent.toString())
            }
        } else {
            watchBatteryIndicator?.setImageLevel(0)
            watchBatteryPercent?.text = getString(R.string.battery_sync_disabled)
        }
    }

    private fun startBatterySyncLastTimeTimer() {
        watchBatteryLastUpdateTimeTimer?.cancel()
        if (batterySyncEnabled) {
            watchBatteryLastUpdateTimeTimer = fixedRateTimer("batterySyncLastTimeTimer", false, 0L, 60 * 1000) {
                updateBatterySyncLastTimeNow()
            }
        } else {
            watchBatteryUpdateNowHolder?.visibility = View.GONE
        }
    }

    private fun updateBatterySyncLastTimeNow() {
        if (batteryPercent > 0) {
            val lastUpdatedMillis = System.currentTimeMillis() - lastUpdateTime
            val lastUpdatedMinutes = TimeUnit.MILLISECONDS.toMinutes(lastUpdatedMillis).toInt()
            val lastUpdatedString = if (lastUpdatedMinutes < 1) {
                getString(R.string.battery_sync_last_updated_under_minute)
            } else if (lastUpdatedMinutes < 60) {
                resources.getQuantityString(R.plurals.battery_sync_last_updated_minutes, lastUpdatedMinutes, lastUpdatedMinutes)
            } else {
                val lastUpdatedHours = TimeUnit.MINUTES.toHours(lastUpdatedMinutes.toLong()).toInt()
                resources.getQuantityString(R.plurals.battery_sync_last_updated_hours, lastUpdatedHours, lastUpdatedHours)
            }
            this@BatterySyncPreferenceWidgetFragment.activity.runOnUiThread {
                watchBatteryLastUpdated?.text = lastUpdatedString
                watchBatteryUpdateNowHolder?.visibility = View.VISIBLE
            }
        }
    }
}
