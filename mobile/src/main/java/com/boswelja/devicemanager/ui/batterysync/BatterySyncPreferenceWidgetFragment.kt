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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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

    private val batteryStatsObserver = Observer<WatchBatteryStats?> {
        updateBatteryStatsDisplay(it)
    }

    private val coroutineScope = MainScope()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var activity: BatterySyncWatchPickerPreferenceActivity
    private lateinit var binding: SettingsWidgetBatterySyncBinding

    private var watchBatteryUpdateTimer: Timer? = null
    private var lastUpdatedRefreshTimerStarted: Boolean = false
    private var batterySyncEnabled: Boolean = false
    private var liveBatteryStats: LiveData<WatchBatteryStats?>? = null
    private var batteryStatsDatabase: WatchBatteryStatsDatabase? = null

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.d("onSharedPreferenceChanged($key) called")
        when (key) {
            BATTERY_SYNC_ENABLED_KEY -> {
                val newBatterySyncState =
                        sharedPreferences?.getBoolean(BATTERY_SYNC_ENABLED_KEY, false) == true
                setBatterySyncState(newBatterySyncState)
            }
        }
    }

    override fun onWatchAdded(watch: Watch) {} // Do nothing

    override fun onConnectedWatchChanging() {} // Do nothing

    override fun onConnectedWatchChanged(isSuccess: Boolean) {
        Timber.d("onConnectedWatchChanged($isSuccess) called")
        if (isSuccess and batterySyncEnabled) {
            setObservingWatchStats(activity.watchConnectionManager?.connectedWatch?.id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate() called")
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
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
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop() called")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        stopBatteryUpdateTimer()
        batteryStatsDatabase?.close()
    }

    private fun setObservingWatchStats(watchId: String?) {
        if (liveBatteryStats != null) {
            liveBatteryStats!!.removeObserver(batteryStatsObserver)
        }
        if (watchId != null) {
            liveBatteryStats = batteryStatsDatabase!!.batteryStatsDao()
                    .getObservableStatsForWatch(watchId).also {
                        it.observe(this, batteryStatsObserver)
                    }
        }
    }

    /**
     * Sets a new Battery Sync state and updates the view accordingly.
     * @param newBatterySyncState The new state of battery sync.
     */
    private fun setBatterySyncState(newBatterySyncState: Boolean) {
        Timber.d("setBatterySyncState($newBatterySyncState) called")
        batterySyncEnabled = newBatterySyncState
        if (batterySyncEnabled) {
            showLoading()
            setObservingWatchStats(activity.watchConnectionManager?.connectedWatch?.id)
        } else {
            updateBatteryStatsDisplay(null)
            stopBatteryUpdateTimer()
            setObservingWatchStats(null)
            coroutineScope.launch(Dispatchers.IO) {
                if (activity.watchConnectionManager?.connectedWatch != null) {
                    batteryStatsDatabase?.batteryStatsDao()
                            ?.deleteStatsForWatch(activity.watchConnectionManager?.connectedWatch!!.id)
                }
            }
        }
    }

    /**
     * Update the watch battery status and indicator displayed in the UI.
     */
    private fun updateBatteryStatusView(batteryStats: WatchBatteryStats?) {
        Timber.d("updateTitleText() called")
        binding.apply {
            if (batterySyncEnabled) {
                if (batteryStats != null && batteryStats.batteryPercent > 0) {
                    watchBatteryIndicator.setImageLevel(batteryStats.batteryPercent)
                    watchBatteryPercent.text = getString(
                            R.string.battery_sync_percent_short,
                            batteryStats.batteryPercent.toString())
                } else {
                    showError()
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
    private fun showLoading() {
        Timber.d("showLoading() called")
        binding.apply {
            watchBatteryIndicator.setImageLevel(0)
            watchBatteryPercent.setText(R.string.battery_sync_loading)
            lastUpdatedTime.visibility = View.GONE
        }
    }

    /**
     * Sets the view to the error state.
     */
    private fun showError() {
        Timber.d("showError() called")
        binding.apply {
            watchBatteryIndicator.setImageLevel(0)
            watchBatteryPercent.setText(R.string.battery_sync_error)
            lastUpdatedTime.visibility = View.GONE
        }
    }

    /**
     * Update the last sync time in the UI.
     */
    private fun updateLastSyncTimeView(batteryStats: WatchBatteryStats?) {
        Timber.d("updateLastSyncTimeView() called")
        if (batterySyncEnabled && batteryStats != null && batteryStats.batteryPercent > 0) {
            val lastUpdatedDiffMillis = System.currentTimeMillis() - batteryStats.lastUpdatedMillis
            val lastUpdatedMinutes = TimeUnit.MILLISECONDS.toMinutes(lastUpdatedDiffMillis).toInt()
            val lastUpdatedString = when {
                lastUpdatedMinutes < 1 -> {
                    getString(R.string.battery_sync_last_updated_under_minute)
                }
                lastUpdatedMinutes < 60 -> {
                    resources.getQuantityString(
                            R.plurals.battery_sync_last_updated_minutes,
                            lastUpdatedMinutes, lastUpdatedMinutes)
                }
                else -> {
                    val lastUpdatedHours =
                            TimeUnit.MINUTES.toHours(lastUpdatedMinutes.toLong()).toInt()
                    resources.getQuantityString(
                            R.plurals.battery_sync_last_updated_hours,
                            lastUpdatedHours, lastUpdatedHours)
                }
            }
            getActivity()?.runOnUiThread {
                binding.apply {
                    lastUpdatedTime.text = lastUpdatedString
                    lastUpdatedTime.visibility = View.VISIBLE
                }
            }
        } else {
            getActivity()?.runOnUiThread {
                binding.apply {
                    lastUpdatedTime.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Start the UI update timer.
     */
    private fun startLastUpdatedRefreshTimer() {
        Timber.d("startLastUpdatedRefreshTimer() called")
        if (!lastUpdatedRefreshTimerStarted) {
            watchBatteryUpdateTimer = fixedRateTimer(
                    BATTERY_SYNC_UPDATE_TIMER_NAME, false, 0,
                    TimeUnit.SECONDS.toMillis(LAST_UPDATED_REFRESH_TIMER_INTERVAL_SECONDS)) {
                updateLastSyncTimeView(liveBatteryStats?.value)
            }
            lastUpdatedRefreshTimerStarted = true
        } else {
            Timber.i("Timer start not needed")
        }
    }

    /**
     * Stop the UI update timer.
     */
    private fun stopBatteryUpdateTimer() {
        Timber.d("stopBatteryUpdateTimer() called")
        if (lastUpdatedRefreshTimerStarted) {
            Timber.i("Stopping battery update timer")
            watchBatteryUpdateTimer?.cancel()
            watchBatteryUpdateTimer?.purge()
            lastUpdatedRefreshTimerStarted = false
        } else {
            Timber.i("Battery update timer not running")
        }
    }

    /**
     * Get an updated [WatchBatteryStats] object and update the UI.
     */
    private fun updateBatteryStatsDisplay(batteryStats: WatchBatteryStats?) {
        Timber.i("Updating widget")
        updateBatteryStatusView(batteryStats)
        if (batteryStats != null) {
            startLastUpdatedRefreshTimer()
        } else {
            updateLastSyncTimeView(batteryStats)
        }
    }

    /**
     * Performs the widget's initial setup. Handles anything that can't be executed in a test.
     */
    fun setupWidget() {
        activity = getActivity() as BatterySyncWatchPickerPreferenceActivity
        coroutineScope.launch(Dispatchers.IO) {
            batteryStatsDatabase = WatchBatteryStatsDatabase.open(requireContext())
            withContext(Dispatchers.Main) {
                setBatterySyncState(batterySyncEnabled)
            }
        }
    }

    companion object {
        private const val BATTERY_SYNC_UPDATE_TIMER_NAME = "batterySyncUpdateTimer"

        /**
         * The interval of the battery sync widget update timer in seconds.
         */
        private const val LAST_UPDATED_REFRESH_TIMER_INTERVAL_SECONDS: Long = 60
    }
}
