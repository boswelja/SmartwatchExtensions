/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.preference.ListPreference
import android.preference.MultiSelectListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.design.widget.Snackbar
import android.widget.Toast
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Utils
import com.boswelja.devicemanager.service.DnDSyncService

class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private lateinit var mainActivity: MainActivity
    private lateinit var grantAdminPermPref: Preference
    private lateinit var batterySyncIntervalPref: ListPreference
    private lateinit var dndSyncWhenPref: MultiSelectListPreference

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            References.GRANT_PERMS_PREF_KEY -> {
                if (!mainActivity.isDeviceAdmin()) {
                    Utils.requestDeviceAdminPerms(context)
                }
                true
            }
            References.BATTERY_SYNC_NOW_KEY -> {
                Utils.updateBatteryStats(context)
                Snackbar.make(view, "Re-synced battery info to watch", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key) {
            References.HIDE_APP_ICON_KEY -> {
                mainActivity.changeAppIconVisibility(newValue!! == true)
                true
            }
            References.BATTERY_SYNC_INTERVAL_KEY -> {
                val listPref = preference as ListPreference
                val value = newValue.toString().toLong()
                preference.summary = listPref.entries[listPref.entryValues.indexOf(value.toString())]
                mainActivity.createBatterySyncJob(value)
                true
            }
            References.BATTERY_SYNC_ENABLED_KEY -> {
                if (newValue!! == true) {
                    mainActivity.createBatterySyncJob(batterySyncIntervalPref.value.toLong())
                } else {
                    mainActivity.stopBatterySyncJob()
                }
                true
            }
            References.DND_SYNC_ENABLED_KEY -> {
                if (newValue!! == true) {
                    AlertDialog.Builder(context)
                            .setTitle("Additional setup required")
                            .setMessage("You need to connect your watch to your PC and execute the following command via ADB: ${context.getString(R.string.dnd_sync_adb_command)}")
                            .setPositiveButton("Done", { _, _ ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    activity.startForegroundService(Intent(context, DnDSyncService::class.java))
                                } else {
                                    activity.startService(Intent(context, DnDSyncService::class.java))
                                }
                            })
                            .setNegativeButton("Cancel", { _, _ ->
                                preference.sharedPreferences.edit().putBoolean(References.DND_SYNC_ENABLED_KEY, false)
                                (preference as SwitchPreference).isChecked = false
                            })
                            .setNeutralButton("Copy to Clipboard", { _, _ ->
                                val clipboardManager = context.getSystemService(ClipboardManager::class.java) as ClipboardManager
                                val clip = ClipData.newPlainText("DnD sync ADB command", context.getString(R.string.dnd_sync_adb_command))
                                clipboardManager.primaryClip = clip
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                (preference as SwitchPreference).isChecked = false
                            })
                            .show()
                } else {
                    (activity.bindService(Intent(context, DnDSyncService::class.java), ServiceConn(), 0))
                }
                true
            }
            References.DND_SYNC_WHEN_KEY -> {
                @Suppress("UNCHECKED_CAST")
                val values = newValue as? Set<String> ?: return true
                val prefs = preference.sharedPreferences
                prefs.edit()
                        .putBoolean(References.DND_SYNC_WHEN_ALARMS_ONLY,
                                values.contains(References.DND_SYNC_WHEN_ALARMS_ONLY))
                        .putBoolean(References.DND_SYNC_WHEN_PRIORITY_ONLY,
                                values.contains(References.DND_SYNC_WHEN_PRIORITY_ONLY))
                        .putBoolean(References.DND_SYNC_WHEN_TOTAL_SILENCE,
                                values.contains(References.DND_SYNC_WHEN_TOTAL_SILENCE))
                        .apply()
                updateDndSyncWhenSummary(values)
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
        addPreferencesFromResource(R.xml.prefs)
        addPreferencesFromResource(R.xml.prefs_battery_sync)
        addPreferencesFromResource(R.xml.prefs_dnd_sync)

        val hideAppIconPref = findPreference(References.HIDE_APP_ICON_KEY)
        hideAppIconPref.onPreferenceChangeListener = this

        grantAdminPermPref = findPreference(References.GRANT_PERMS_PREF_KEY)
        grantAdminPermPref.onPreferenceClickListener = this

        batterySyncIntervalPref = findPreference(References.BATTERY_SYNC_INTERVAL_KEY) as ListPreference
        batterySyncIntervalPref.onPreferenceChangeListener = this
        batterySyncIntervalPref.summary = batterySyncIntervalPref.entry

        val batterySyncEnabledPref = findPreference(References.BATTERY_SYNC_ENABLED_KEY)
        batterySyncEnabledPref.onPreferenceChangeListener = this

        val batterySyncForcePref = findPreference(References.BATTERY_SYNC_NOW_KEY)
        batterySyncForcePref.onPreferenceClickListener = this

        val dndSyncEnabledPref = findPreference(References.DND_SYNC_ENABLED_KEY)
        dndSyncEnabledPref.onPreferenceChangeListener = this
        dndSyncEnabledPref.onPreferenceClickListener = this

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            dndSyncWhenPref = findPreference(References.DND_SYNC_WHEN_KEY) as MultiSelectListPreference
            dndSyncWhenPref.onPreferenceChangeListener = this
            updateDndSyncWhenSummary(dndSyncWhenPref.values)
        }
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

    private fun updateDndSyncWhenSummary(setValues: Set<String>) {
        val names = dndSyncWhenPref.entries
        val allEntries = dndSyncWhenPref.entryValues
        val labels = ArrayList<String>()
        if (setValues.contains(References.DND_SYNC_WHEN_PRIORITY_ONLY)) {
            val index = allEntries.indexOf(References.DND_SYNC_WHEN_PRIORITY_ONLY)
            if (index > -1)
                labels.add(names[index].toString())
        }
        if (setValues.contains(References.DND_SYNC_WHEN_ALARMS_ONLY)) {
            val index = allEntries.indexOf(References.DND_SYNC_WHEN_ALARMS_ONLY)
            if (index > -1)
                labels.add(names[index].toString())
        }
        if (setValues.contains(References.DND_SYNC_WHEN_TOTAL_SILENCE)) {
            val index = allEntries.indexOf(References.DND_SYNC_WHEN_TOTAL_SILENCE)
            if (index > -1)
                labels.add(names[index].toString())
        }
        val summary =
                when (labels.size) {
            1 -> {
                labels[0]
            }
            2 -> {
                "${labels[0]} or ${labels[1]}"
            }
            3 -> {
                "${labels[0]}, ${labels[1]} or ${labels[2]}"
            }
            else -> {
                "Nothing"
            }
        }
        dndSyncWhenPref.summary = summary
    }

    inner class ServiceConn : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            (service as DnDSyncService.ServiceBinder).getService().destroy()
        }
    }
}