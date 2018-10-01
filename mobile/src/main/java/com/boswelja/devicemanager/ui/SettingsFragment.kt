/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.DnDHandler
import com.boswelja.devicemanager.common.PreferenceKey
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private lateinit var mainActivity: MainActivity
    private lateinit var grantAdminPermPref: Preference
    private lateinit var batterySyncIntervalPref: ListPreference
    private lateinit var dndSyncPhoneToWatchPref: CheckBoxPreference
    private lateinit var dndSyncWatchToPhonePref: CheckBoxPreference
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            PreferenceKey.GRANT_PERMS_PREF_KEY -> {
                if (!mainActivity.isDeviceAdmin()) {
                    Utils.requestDeviceAdminPerms(context!!)
                }
                true
            }
            PreferenceKey.BATTERY_SYNC_NOW_KEY -> {
                Utils.updateBatteryStats(context!!)
                Snackbar.make(view!!, "Re-synced battery info to watch", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key) {
            PreferenceKey.HIDE_APP_ICON_KEY -> {
                mainActivity.changeAppIconVisibility(newValue!! == true)
                true
            }
            PreferenceKey.BATTERY_SYNC_INTERVAL_KEY -> {
                val listPref = preference as ListPreference
                val value = newValue.toString().toLong()
                listPref.summary = listPref.entries[listPref.entryValues.indexOf(value.toString())]
                mainActivity.createBatterySyncJob(value)
                true
            }
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                if (newValue!! == true) {
                    mainActivity.createBatterySyncJob(batterySyncIntervalPref.value.toLong())
                } else {
                    mainActivity.stopBatterySyncJob()
                }
                true
            }
            PreferenceKey.DND_SYNC_ENABLED_KEY -> {
                if (newValue!! == true) {
                    //TODO Actually check if watch has correct permissions
                    AlertDialog.Builder(context)
                            .setTitle("Additional setup required")
                            .setMessage("You need to connect your watch to your PC and execute the following command via ADB: ${context!!.getString(R.string.dnd_sync_adb_command)}")
                            .setPositiveButton("Done") { _, _ ->
                                preference.sharedPreferences.edit().putBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, true)
                                (preference as SwitchPreference).isChecked = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    activity?.startForegroundService(Intent(context, DnDHandler::class.java))
                                } else {
                                    activity?.startService(Intent(context, DnDHandler::class.java))
                                }
                                Utils.updateWatchPrefs(context!!)
                            }
                            .setNegativeButton("Cancel") { _, _ ->
                                preference.sharedPreferences.edit().putBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, false)
                                (preference as SwitchPreference).isChecked = false
                                Utils.updateWatchPrefs(context!!)
                            }
                            .setNeutralButton("Copy to Clipboard") { _, _ ->
                                preference.sharedPreferences.edit().putBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, false)
                                (preference as SwitchPreference).isChecked = false
                                val clipboardManager = context!!.getSystemService(ClipboardManager::class.java) as ClipboardManager
                                val clip = ClipData.newPlainText("DnD sync ADB command", context!!.getString(R.string.dnd_sync_adb_command))
                                clipboardManager.primaryClip = clip
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                Utils.updateWatchPrefs(context!!)
                            }
                            .show()
                } else {
                    preference.sharedPreferences.edit().putBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, false)
                    (preference as SwitchPreference).isChecked = false
                    Utils.updateWatchPrefs(context!!)
                }
                false
            }
            PreferenceKey.DND_SYNC_SEND_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                dndSyncPhoneToWatchPref.isChecked = value
                updateDndPhoneToWatchSyncSummary()
                Utils.updateWatchPrefs(context!!)
                false
            }
            PreferenceKey.DND_SYNC_RECEIVE_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                dndSyncWatchToPhonePref.isChecked = value
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    updateDndWatchToPhoneSyncSummary()
                    Utils.updateWatchPrefs(context!!)
                } else {
                    Toast.makeText(context, "Please grant Wearable Extensions permission to change Do Not Disturb state", Toast.LENGTH_SHORT).show()
                    startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 12345)
                }
                false
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        notificationManager = context?.getSystemService(NotificationManager::class.java)!!
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        mainActivity = activity as MainActivity

        addPreferencesFromResource(R.xml.prefs)
        addPreferencesFromResource(R.xml.prefs_battery_sync)
        addPreferencesFromResource(R.xml.prefs_dnd_sync)

        val hideAppIconPref = findPreference(PreferenceKey.HIDE_APP_ICON_KEY)
        hideAppIconPref.onPreferenceChangeListener = this

        grantAdminPermPref = findPreference(PreferenceKey.GRANT_PERMS_PREF_KEY)
        grantAdminPermPref.onPreferenceClickListener = this

        batterySyncIntervalPref = findPreference(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY) as ListPreference
        batterySyncIntervalPref.onPreferenceChangeListener = this
        batterySyncIntervalPref.summary = batterySyncIntervalPref.entry

        val batterySyncEnabledPref = findPreference(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)
        batterySyncEnabledPref.onPreferenceChangeListener = this

        val batterySyncForcePref = findPreference(PreferenceKey.BATTERY_SYNC_NOW_KEY)
        batterySyncForcePref.onPreferenceClickListener = this

        val dndSyncEnabledPref = findPreference(PreferenceKey.DND_SYNC_ENABLED_KEY)
        dndSyncEnabledPref.onPreferenceChangeListener = this
        dndSyncEnabledPref.onPreferenceClickListener = this

        dndSyncPhoneToWatchPref = findPreference(PreferenceKey.DND_SYNC_SEND_KEY) as CheckBoxPreference
        dndSyncPhoneToWatchPref.onPreferenceChangeListener = this
        updateDndPhoneToWatchSyncSummary()

        dndSyncWatchToPhonePref = findPreference(PreferenceKey.DND_SYNC_RECEIVE_KEY) as CheckBoxPreference
        dndSyncWatchToPhonePref.onPreferenceChangeListener = this
        updateDndWatchToPhoneSyncSummary()
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

    private fun updateDndPhoneToWatchSyncSummary() {
        val syncPhoneToWatch = sharedPrefs.getBoolean(PreferenceKey.DND_SYNC_SEND_KEY, true)
        if (syncPhoneToWatch) {
            dndSyncPhoneToWatchPref.summary = "Syncing DnD state from your phone to your watch"
        } else {
            dndSyncPhoneToWatchPref.summary = "Not syncing DnD state from your phone to your watch"
        }
    }

    private fun updateDndWatchToPhoneSyncSummary() {
        val syncWatchToPhone = sharedPrefs.getBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, false)
        if (syncWatchToPhone) {
            dndSyncWatchToPhonePref.summary = "Syncing DnD state from your watch to your phone"
        } else {
            dndSyncWatchToPhonePref.summary = "Not syncing DnD state from your watch to your phone"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 12345) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                updateDndWatchToPhoneSyncSummary()
                Utils.updateWatchPrefs(context!!)
            } else {
                sharedPrefs.edit().putBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, false).apply()
            }
        }
    }
}