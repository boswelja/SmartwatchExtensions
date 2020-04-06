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
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.BatterySyncJob
import com.boswelja.devicemanager.batterysync.WatchBatteryUpdateReceiver
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.ui.batterysync.Utils.updateBatteryStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatterySyncPreferenceFragment :
        BasePreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    private val coroutineScope = MainScope()

    private lateinit var batterySyncEnabledPreference: SwitchPreference
    private lateinit var batterySyncIntervalPreference: SeekBarPreference
    private lateinit var batterySyncPhoneChargedNotiPreference: CheckBoxPreference
    private lateinit var batterySyncWatchChargedNotiPreference: CheckBoxPreference
    private lateinit var batteryChargeThresholdPreference: SeekBarPreference

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BATTERY_SYNC_ENABLED_KEY -> {
                batterySyncEnabledPreference.isChecked = sharedPreferences?.getBoolean(key, false)!!
                setBatteryChargeThresholdEnabled()
            }
            BATTERY_PHONE_CHARGE_NOTI_KEY -> {
                batterySyncPhoneChargedNotiPreference.isChecked = sharedPreferences?.getBoolean(key, false)!!
                setBatteryChargeThresholdEnabled()
            }
            BATTERY_WATCH_CHARGE_NOTI_KEY -> {
                batterySyncWatchChargedNotiPreference.isChecked = sharedPreferences?.getBoolean(key, false)!!
                setBatteryChargeThresholdEnabled()
            }
            BATTERY_CHARGE_THRESHOLD_KEY -> {
                updateChargeNotiPrefSummaries()
                coroutineScope.launch {
                    getWatchConnectionManager()?.updatePreferenceOnWatch(key)
                }
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (val key = preference?.key) {
            BATTERY_SYNC_ENABLED_KEY -> {
                val newBool = newValue == true
                setBatteryChargeThresholdEnabled()
                if (newBool) {
                    coroutineScope.launch {
                        val success = BatterySyncJob.startJob(activity.watchConnectionManager)
                        if (success) {
                            sharedPreferences.edit().putBoolean(key, newBool).apply()
                            getWatchConnectionManager()?.updatePreferenceOnWatch(key)
                            updateBatteryStats(context!!, activity.watchConnectionManager?.getConnectedWatchId())
                        } else {
                            withContext(Dispatchers.Main) {
                                activity.createSnackBar(getString(R.string.battery_sync_enable_failed))
                            }
                        }
                    }
                } else {
                    sharedPreferences.edit().putBoolean(key, newBool).apply()
                    coroutineScope.launch {
                        getWatchConnectionManager()?.updatePreferenceOnWatch(key)
                        BatterySyncJob.stopJob(activity.watchConnectionManager)
                        withContext(Dispatchers.IO) {
                            (activity as BatterySyncPreferenceActivity).batteryStatsDatabase?.batteryStatsDao()?.deleteStatsForWatch(activity.watchConnectionManager?.getConnectedWatchId()!!)
                        }
                    }
                }
                false
            }
            BATTERY_SYNC_INTERVAL_KEY -> {
                val value = (newValue as Int)
                sharedPreferences.edit().putInt(key, value).apply()
                coroutineScope.launch {
                    activity.watchConnectionManager?.updatePrefInDatabase(key, value)
                }
                batterySyncIntervalPreference.value = value
                coroutineScope.launch {
                    BatterySyncJob.startJob(activity.watchConnectionManager)
                }
                false
            }
            BATTERY_PHONE_CHARGE_NOTI_KEY,
            BATTERY_WATCH_CHARGE_NOTI_KEY -> {
                val value = newValue == true
                sharedPreferences.edit().putBoolean(key, value).apply()
                coroutineScope.launch {
                    getWatchConnectionManager()?.updatePreferenceOnWatch(key)
                }
                false
            }
            else -> true
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (preferenceManager.sharedPreferences.getBoolean(BATTERY_WATCH_CHARGE_NOTI_KEY, false) &&
                !Compat.areNotificationsEnabled(context!!, WatchBatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANNEL_ID)) {
            preferenceManager.sharedPreferences.edit()
                    .putBoolean(BATTERY_WATCH_CHARGE_NOTI_KEY, false)
                    .apply()
            activity.createSnackBar(getString(R.string.battery_sync_watch_charged_noti_channel_disabled))
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_battery_sync)

        batterySyncEnabledPreference = findPreference(BATTERY_SYNC_ENABLED_KEY)!!
        batterySyncIntervalPreference = findPreference(BATTERY_SYNC_INTERVAL_KEY)!!
        batterySyncPhoneChargedNotiPreference = findPreference(BATTERY_PHONE_CHARGE_NOTI_KEY)!!
        batterySyncWatchChargedNotiPreference = findPreference(BATTERY_WATCH_CHARGE_NOTI_KEY)!!
        batteryChargeThresholdPreference = findPreference(BATTERY_CHARGE_THRESHOLD_KEY)!!

        batterySyncEnabledPreference.onPreferenceChangeListener = this
        batterySyncIntervalPreference.onPreferenceChangeListener = this
        batterySyncPhoneChargedNotiPreference.onPreferenceChangeListener = this
        batterySyncWatchChargedNotiPreference.onPreferenceChangeListener = this

        updateChargeNotiPrefSummaries()
        setBatteryChargeThresholdEnabled()
    }

    private fun updateChargeNotiPrefSummaries() {
        val chargeThreshold = batteryChargeThresholdPreference.value
        batterySyncPhoneChargedNotiPreference.summary = getString(R.string.pref_battery_sync_phone_charged_noti_summary).format(chargeThreshold)
        batterySyncWatchChargedNotiPreference.summary = getString(R.string.pref_battery_sync_watch_charged_noti_summary).format(chargeThreshold)
    }

    private fun setBatteryChargeThresholdEnabled() {
        val sharedPreferences = batteryChargeThresholdPreference.sharedPreferences
        batteryChargeThresholdPreference.isEnabled =
                sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false) &&
                        (sharedPreferences.getBoolean(BATTERY_PHONE_CHARGE_NOTI_KEY, false) ||
                                sharedPreferences.getBoolean(BATTERY_WATCH_CHARGE_NOTI_KEY, false))
    }
}
