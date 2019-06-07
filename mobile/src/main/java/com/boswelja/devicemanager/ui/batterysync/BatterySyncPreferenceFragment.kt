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
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.BatteryUpdateJob
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.common.batterysync.BatteryUpdateReceiver
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncLayer
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.widget.WatchBatteryWidget
import java.util.concurrent.TimeUnit

class BatterySyncPreferenceFragment :
        BasePreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    private lateinit var preferenceSyncLayer: PreferenceSyncLayer

    private lateinit var batterySyncEnabledPreference: SwitchPreference
    private lateinit var batterySyncIntervalPreference: SeekBarPreference
    private lateinit var batterySyncPhoneChargedNotiPreference: CheckBoxPreference
    private lateinit var batterySyncWatchChargedNotiPreference: CheckBoxPreference
    private lateinit var batteryChargeThresholdPreference: SeekBarPreference

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BATTERY_SYNC_ENABLED_KEY -> {
                val newValue = sharedPreferences?.getBoolean(key, false)!!
                batterySyncEnabledPreference.isChecked = newValue
                setBatteryChargeThresholdEnabled()
                if (newValue) {
                    BatteryUpdateJob.startJob(context!!)
                    updateBatteryStats(context!!, References.CAPABILITY_WATCH_APP)
                } else {
                    BatteryUpdateJob.stopJob(context!!)
                }
                preferenceSyncLayer.pushNewData()
                WatchBatteryWidget.updateWidgets(context!!)
            }
            BATTERY_PHONE_CHARGE_NOTI_KEY -> {
                batterySyncPhoneChargedNotiPreference.isChecked = sharedPreferences?.getBoolean(key, false)!!
                setBatteryChargeThresholdEnabled()
            }
            BATTERY_WATCH_CHARGE_NOTI_KEY -> {
                batterySyncWatchChargedNotiPreference.isChecked = sharedPreferences?.getBoolean(key, false)!!
                setBatteryChargeThresholdEnabled()
            }
            BATTERY_CHARGE_THRESHOLD_KEY -> updateChargeNotiPrefSummaries()
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key!!) {
            BATTERY_SYNC_INTERVAL_KEY -> {
                val value = (newValue as Int).toLong()
                val syncTimeMillis = TimeUnit.MINUTES.toMillis(value)
                BatteryUpdateJob.startJob(context!!, syncTimeMillis)
                true
            }
            BATTERY_PHONE_CHARGE_NOTI_KEY,
            BATTERY_WATCH_CHARGE_NOTI_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                preferenceSyncLayer.pushNewData()
                false
            }
            BATTERY_CHARGE_THRESHOLD_KEY -> {
                true
            }
            else -> true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceSyncLayer = PreferenceSyncLayer(context!!)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (preferenceManager.sharedPreferences.getBoolean(BATTERY_WATCH_CHARGE_NOTI_KEY, false) &&
                !Compat.notificationsEnabled(context!!, BatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANEL_ID)) {
            preferenceManager.sharedPreferences.edit()
                    .putBoolean(BATTERY_WATCH_CHARGE_NOTI_KEY, false)
                    .apply()
            (activity as BasePreferenceActivity).createSnackBar(getString(R.string.battery_sync_watch_charged_noti_channel_disabled))
            preferenceSyncLayer.pushNewData()
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
        batteryChargeThresholdPreference.onPreferenceChangeListener = this

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
