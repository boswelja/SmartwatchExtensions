package com.boswelja.devicemanager.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.design.widget.Snackbar
import android.util.Log
import com.boswelja.devicemanager.common.Config
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Utils
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class SettingsFragment: PreferenceFragment(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private lateinit var mainActivity: MainActivity
    private lateinit var grantAdminPermPref: Preference
    private lateinit var batterySyncIntervalPref: ListPreference

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            Config.GRANT_PERMS_PREF_KEY -> {
                if (!mainActivity.isDeviceAdmin()) {
                    Utils.requestDeviceAdminPerms(context)
                }
                true
            }
            Config.BATTERY_SYNC_NOW_KEY -> {
                val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = mainActivity.registerReceiver(null, ifilter)
                val batteryPct = ((batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / (batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat())) * 100).toInt()
                Log.d("BatteryInfoUpdate", batteryPct.toString())

                val dataClient = Wearable.getDataClient(context)
                val putDataMapReq = PutDataMapRequest.create("/batteryPercent")
                putDataMapReq.dataMap.putInt("com.boswelja.devicemanager.batterypercent", batteryPct)
                val putDataReq = putDataMapReq.asPutDataRequest()
                Log.d("BatteryInfoUpdate", putDataReq.uri.toString())
                dataClient.putDataItem(putDataReq)
                Snackbar.make(view, "Re-synced battery info to watch", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key) {
            Config.HIDE_APP_ICON_KEY -> {
                mainActivity.changeAppIconVisibility(newValue!! == true)
                true
            }
            Config.BATTERY_SYNC_INTERVAL_KEY -> {
                val listPref = preference as ListPreference
                val value = newValue.toString().toLong()
                preference.summary = listPref.entries[listPref.entryValues.indexOf(value.toString())]
                mainActivity.createBatterySyncJob(value)
                true
            }
            Config.BATTERY_SYNC_ENABLED_KEY -> {
                if (newValue!! == true) {
                    mainActivity.createBatterySyncJob(batterySyncIntervalPref.value.toLong())
                } else {
                    mainActivity.stopBatterySyncJob()
                }
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
        addPreferencesFromResource(R.xml.prefs)

        val hideAppIconPref = findPreference(Config.HIDE_APP_ICON_KEY)
        hideAppIconPref.onPreferenceChangeListener = this

        grantAdminPermPref = findPreference(Config.GRANT_PERMS_PREF_KEY)
        grantAdminPermPref.onPreferenceClickListener = this

        batterySyncIntervalPref= findPreference(Config.BATTERY_SYNC_INTERVAL_KEY) as ListPreference
        batterySyncIntervalPref.onPreferenceChangeListener = this
        batterySyncIntervalPref.summary = batterySyncIntervalPref.entry

        val batterySyncEnabledPref = findPreference(Config.BATTERY_SYNC_ENABLED_KEY)
        batterySyncEnabledPref.onPreferenceChangeListener = this

        val batterySyncForcePref = findPreference(Config.BATTERY_SYNC_NOW_KEY)
        batterySyncForcePref.onPreferenceClickListener = this
    }

    override fun onResume() {
        super.onResume()
        updateAdminSummary()
    }

    fun updateAdminSummary() {
        if (mainActivity.isDeviceAdmin()) {
            grantAdminPermPref.summary = getString(R.string.pref_admin_perms_summary_granted)
        } else {
            grantAdminPermPref.summary = getString(R.string.pref_admin_perms_summary_missing)
        }
    }
}