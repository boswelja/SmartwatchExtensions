package com.boswelja.devicemanager.ui.batterysync

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_LAST_WHEN_KEY
import com.boswelja.devicemanager.common.References
import java.util.concurrent.TimeUnit

class BatterySyncPreferenceWidgetFragment :
        Fragment(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var watchBatteryIndicator: AppCompatImageView
    private lateinit var watchBatteryPercent: AppCompatTextView
    private lateinit var watchBatteryLastUpdated: AppCompatTextView
    private lateinit var watchBatteryUpdateNowHolder: View

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BATTERY_PERCENT_KEY -> updateWatchBatteryPercent()
            BATTERY_SYNC_LAST_WHEN_KEY -> updateWatchBatterySyncLastTime()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        updateWatchBatteryPercent()
        updateWatchBatterySyncLastTime()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_widget_battery_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        watchBatteryIndicator = view.findViewById(R.id.watch_battery_indicator)
        watchBatteryPercent = view.findViewById(R.id.watch_battery_percent)
        watchBatteryLastUpdated = view.findViewById(R.id.last_updated_time)

        watchBatteryUpdateNowHolder = view.findViewById<View>(R.id.updated_time_holder)
        watchBatteryUpdateNowHolder.setOnClickListener {
            if (sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)) {
                CommonUtils.updateBatteryStats(context!!, References.CAPABILITY_WATCH_APP)
            }
        }
    }

    private fun updateWatchBatteryPercent() {
        val batteryPercent = sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0)
        if (batteryPercent > 0) {
            watchBatteryIndicator.setImageLevel(batteryPercent)
            watchBatteryPercent.text = getString(R.string.battery_sync_percent_short, batteryPercent.toString())
        } else {
            watchBatteryIndicator.setImageLevel(0)
            watchBatteryPercent.text = getString(R.string.battery_sync_disabled)
        }
    }

    private fun updateWatchBatterySyncLastTime() {
        val batteryPercent = sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0)
        if (batteryPercent > 0) {
            val lastUpdatedMilliseconds = System.currentTimeMillis() - sharedPreferences.getLong(BATTERY_SYNC_LAST_WHEN_KEY, 0)
            val lastUpdatedTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(lastUpdatedMilliseconds).toInt()
            val lastUpdatedString = when {
                lastUpdatedTimeMinutes < 1 -> getString(R.string.battery_sync_last_updated_under_minute)
                lastUpdatedTimeMinutes == 1 -> getString(R.string.battery_sync_last_updated_minute, lastUpdatedTimeMinutes.toString())
                else -> getString(R.string.battery_sync_last_updated_minutes, lastUpdatedTimeMinutes.toString())
            }
            watchBatteryLastUpdated.text = lastUpdatedString
            watchBatteryUpdateNowHolder.visibility = View.VISIBLE
        } else {
            watchBatteryUpdateNowHolder.visibility = View.INVISIBLE
        }
    }
}