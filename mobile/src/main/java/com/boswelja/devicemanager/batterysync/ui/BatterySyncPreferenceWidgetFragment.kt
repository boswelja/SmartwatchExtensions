/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.databinding.SettingsWidgetBatterySyncBinding
import com.boswelja.devicemanager.watchmanager.SelectedWatchHandler
import java.util.concurrent.TimeUnit
import timber.log.Timber

class BatterySyncPreferenceWidgetFragment : Fragment() {

    private val viewModel: BatterySyncViewModel by activityViewModels()
    private val batteryStatsObserver =
        Observer<WatchBatteryStats?> {
            if (it != null) {
                updateBatteryStats(it)
            } else {
                Timber.w("Battery stats null")
            }
        }

    private val selectedWatchHandler by lazy { SelectedWatchHandler(requireContext()) }
    private lateinit var lastUpdateTimer: LastUpdateTimer

    private lateinit var binding: SettingsWidgetBatterySyncBinding

    private var batteryStatsObservable: LiveData<WatchBatteryStats?>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView() called")
        binding = SettingsWidgetBatterySyncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lastUpdateTimer = LastUpdateTimer(lifecycle)
        lastUpdateTimer.totalMinutes.observe(viewLifecycleOwner) { setLastUpdateTime(it) }

        selectedWatchHandler.selectedWatch.observe(viewLifecycleOwner) { watch ->
            watch?.id?.let { setObservingWatchStats(it) }
        }
        viewModel.batterySyncEnabled.observe(viewLifecycleOwner) {
            if (!it) showBatterySyncDisabled()
        }
    }

    private fun setObservingWatchStats(watchId: String) {
        batteryStatsObservable?.removeObserver(batteryStatsObserver)
        batteryStatsObservable = viewModel.getBatteryStatsObservable(watchId)
        batteryStatsObservable!!.observe(viewLifecycleOwner, batteryStatsObserver)
    }

    private fun showBatterySyncDisabled() {
        lastUpdateTimer.stopTimer()
        binding.apply {
            watchBatteryIndicator.setImageLevel(0)
            watchBatteryPercent.setText(R.string.battery_sync_disabled)
            lastUpdatedTime.text = null
        }
    }

    private fun updateBatteryStats(batteryStats: WatchBatteryStats) {
        val dataAgeMinutes =
            TimeUnit.MILLISECONDS
                .toMinutes(System.currentTimeMillis() - batteryStats.lastUpdatedMillis)
                .toInt()
        lastUpdateTimer.resetTimer(dataAgeMinutes)
        binding.apply {
            watchBatteryIndicator.setImageLevel(batteryStats.percent)
            watchBatteryPercent.text =
                getString(R.string.battery_sync_percent_short, batteryStats.percent.toString())
        }
    }

    private fun setLastUpdateTime(minutes: Int) {
        val lastUpdateText =
            if (minutes < 1) {
                getString(R.string.battery_sync_last_updated_under_minute)
            } else {
                resources.getQuantityString(
                    R.plurals.battery_sync_last_updated_minutes, minutes, minutes
                )
            }
        binding.lastUpdatedTime.text = lastUpdateText
    }
}
