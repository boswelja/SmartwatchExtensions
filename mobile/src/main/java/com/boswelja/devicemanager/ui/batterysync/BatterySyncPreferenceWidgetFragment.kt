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
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.databinding.SettingsWidgetBatterySyncBinding
import com.boswelja.devicemanager.watchmanager.Watch
import com.boswelja.devicemanager.watchmanager.WatchConnectionListener
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BatterySyncPreferenceWidgetFragment :
        Fragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        WatchConnectionListener {

    private val coroutineScope = MainScope()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var activity: BatterySyncPreferenceActivity
    private lateinit var binding: SettingsWidgetBatterySyncBinding

    private var watchBatteryUpdateTimer: Timer? = null
    private var watchBatteryUpdateTimerStarted: Boolean = false
    private var batterySyncEnabled: Boolean = false
    private var batteryStats: WatchBatteryStats? = null
    private var batteryStatsDatabase: WatchBatteryStatsDatabase? = null

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.d("onSharedPreferenceChanged($key) called")
        when (key) {
            BATTERY_SYNC_ENABLED_KEY -> {
                val newBatterySyncState = sharedPreferences?.getBoolean(BATTERY_SYNC_ENABLED_KEY, false) == true
                setBatterySyncState(newBatterySyncState)
            }
        }
    }

    override fun onWatchAdded(watch: Watch) {} // Do nothing

    override fun onConnectedWatchChanging() {} // Do nothing

    override fun onConnectedWatchChanged(isSuccess: Boolean) {
        Timber.d("onConnectedWatchChanged($isSuccess) called")
        if (isSuccess and batterySyncEnabled) {
            updateBatteryStatsDisplay()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate() called")

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        activity = getActivity() as BatterySyncPreferenceActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Timber.d("onCreateView() called")
        binding = DataBindingUtil.inflate(inflater, R.layout.settings_widget_battery_sync, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart() called")
        batterySyncEnabled = sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        coroutineScope.launch(Dispatchers.IO) {
            batteryStatsDatabase = WatchBatteryStatsDatabase.open(requireContext())
            withContext(Dispatchers.Main) {
                setBatterySyncState(batterySyncEnabled)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop() called")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        stopBatteryUpdateTimer()
        batteryStatsDatabase?.close()
    }

    /**
     * Sets a new Battery Sync state and updates the view accordingly.
     * @param newBatterySyncState The new state of battery sync.
     */
    private fun setBatterySyncState(newBatterySyncState: Boolean) {
        Timber.d("setBatterySyncState() called")
        batterySyncEnabled = newBatterySyncState
        Timber.i("Setting new battery sync state to $newBatterySyncState")
        if (batterySyncEnabled) {
            setWatchBatteryLoading()
            startBatteryUpdateTimer()
        } else {
            updateBatteryStatsDisplay()
            stopBatteryUpdateTimer()
            coroutineScope.launch(Dispatchers.IO) {
                if (activity.watchConnectionManager?.connectedWatch != null) {
                    batteryStatsDatabase?.batteryStatsDao()
                            ?.deleteStatsForWatch(activity.watchConnectionManager?.connectedWatch!!.id)
                }
            }
        }
    }

    /**
     * Update the watch battery percent displayed in the UI.
     */
    private fun updateWatchBatteryPercent() {
        Timber.i("Updating watch battery percent")
        binding.apply {
            if (batterySyncEnabled) {
                if (batteryStats != null && batteryStats!!.batteryPercent > 0) {
                    watchBatteryIndicator.setImageLevel(batteryStats!!.batteryPercent)
                    watchBatteryPercent.text = getString(
                            R.string.battery_sync_percent_short,
                            batteryStats!!.batteryPercent.toString())
                } else {
                    watchBatteryPercent.setText(R.string.battery_sync_error)
                }
            } else {
                watchBatteryIndicator.setImageLevel(0)
                watchBatteryPercent.setText(R.string.battery_sync_disabled)
            }
        }
    }

    /**
     * Sets the view to the loading state.
     */
    private fun setWatchBatteryLoading() {
        Timber.i("Setting to loading state")
        binding.apply {
            watchBatteryIndicator.setImageLevel(0)
            watchBatteryPercent.setText(R.string.battery_sync_loading)
            lastUpdatedTime.visibility = View.GONE
        }
    }

    /**
     * Update the last sync time in the UI.
     */
    private fun updateBatterySyncLastTimeNow() {
        Timber.i("Updating last battery sync time.")
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

    /**
     * Start the UI update timer.
     */
    private fun startBatteryUpdateTimer() {
        Timber.d("startBatteryUpdateTimer() called")
        if (!watchBatteryUpdateTimerStarted && batterySyncEnabled) {
            Timber.i("Starting battery update timer")
            watchBatteryUpdateTimer = fixedRateTimer(
                    BATTERY_SYNC_UPDATE_TIMER_NAME, false,
                    TimeUnit.SECONDS.toMillis(BATTERY_SYNC_UPDATE_TIMER_DELAY),
                    TimeUnit.SECONDS.toMillis(BATTERY_SYNC_UPDATE_TIMER_INTERVAL)) {
                updateBatteryStatsDisplay()
            }
            watchBatteryUpdateTimerStarted = true
        } else {
            Timber.i("Timer start not needed")
        }
    }

    /**
     * Stop the UI update timer.
     */
    private fun stopBatteryUpdateTimer() {
        Timber.d("stopBatteryUpdateTimer() called")
        if (watchBatteryUpdateTimerStarted) {
            Timber.i("Stopping battery update timer")
            watchBatteryUpdateTimer?.cancel()
            watchBatteryUpdateTimer?.purge()
            watchBatteryUpdateTimerStarted = false
        } else {
            Timber.i("Battery update timer not running")
        }
    }

    /**
     * Get an updated [WatchBatteryStats] object and update the UI.
     */
    private fun updateBatteryStatsDisplay() {
        Timber.i("Updating widget")
        coroutineScope.launch(Dispatchers.IO) {
            if (activity.watchConnectionManager?.connectedWatch != null) {
                batteryStats = batteryStatsDatabase
                        ?.batteryStatsDao()
                        ?.getStatsForWatch(activity.watchConnectionManager?.connectedWatch!!.id)
            }

            withContext(Dispatchers.Main) {
                updateWatchBatteryPercent()
                updateBatterySyncLastTimeNow()
            }
        }
    }

    companion object {
        private const val BATTERY_SYNC_UPDATE_TIMER_NAME = "batterySyncUpdateTimer"

        /**
         * The initial delay of the battery sync widget update timer in seconds.
         */
        private const val BATTERY_SYNC_UPDATE_TIMER_DELAY: Long = 3

        /**
         * The interval of the battery sync widget update timer in seconds.
         */
        private const val BATTERY_SYNC_UPDATE_TIMER_INTERVAL: Long = 60
    }
}
