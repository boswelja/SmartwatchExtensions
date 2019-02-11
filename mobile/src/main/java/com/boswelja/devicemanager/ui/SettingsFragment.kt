/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.preference.*
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.DnDHandler
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPrefFragment
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPreference
import com.boswelja.devicemanager.preference.seekbardialog.SeekbarDialogPrefFragment
import com.boswelja.devicemanager.preference.seekbardialog.SeekbarDialogPreference
import com.boswelja.devicemanager.widget.WatchBatteryWidget
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private lateinit var mainActivity: MainActivity
    private lateinit var notiSettingsPref: Preference
    private lateinit var lockPhoneEnabledPref: SwitchPreference
    private lateinit var dndSyncPhoneToWatchPref: CheckBoxPreference
    private lateinit var dndSyncWatchToPhonePref: CheckBoxPreference
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var notificationManager: NotificationManager
    private var isGrantingAdminPerms = false

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            PreferenceKey.BATTERY_SYNC_NOW_KEY -> {
                CommonUtils.updateBatteryStats(context!!)
                Snackbar.make(view!!, getString(R.string.pref_battery_sync_resync_complete), Snackbar.LENGTH_SHORT).show()
                true
            }
            PreferenceKey.NOTIFICATION_SETTINGS_KEY -> {
                val settingsIntent = Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("android.provider.extra.APP_PACKAGE", context!!.packageName)
                        .putExtra("app_package", context!!.packageName)
                        .putExtra("app_uid", context!!.applicationInfo.uid)
                startActivity(settingsIntent)
                true
            }
            "pref_donate" -> {
                DonationDialogFragment().show(activity?.supportFragmentManager!!, "DonationDialog")
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
            PreferenceKey.LOCK_PHONE_ENABLED -> {
                val value = newValue == true
                if (!mainActivity.isDeviceAdmin()) {
                    AlertDialog.Builder(context!!)
                            .setTitle(R.string.grant_device_admin_perm_dialog_title)
                            .setMessage(R.string.grant_device_admin_perm_dialog_message)
                            .setPositiveButton(R.string.dialog_button_grant) { _, _ ->
                                isGrantingAdminPerms = true
                                Utils.requestDeviceAdminPerms(context!!)
                            }
                            .setNegativeButton(R.string.dialog_button_cancel) { _, _ ->
                                preference.sharedPreferences.edit()
                                        .putBoolean(preference.key, false)
                                        .apply()
                                (preference as SwitchPreference).isChecked = false
                            }
                            .show()
                } else {
                    preference.sharedPreferences.edit()
                            .putBoolean(preference.key, value)
                            .apply()
                    (preference as SwitchPreference).isChecked = value
                    Utils.updateWatchPrefs(context!!)
                }
                false
            }
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                val sharedPrefs = preference.sharedPreferences
                val value = newValue == true
                sharedPrefs.edit().putBoolean(preference.key, value).apply()
                (preference as SwitchPreference).isChecked = value
                if (value) {
                    mainActivity.createBatterySyncJob(sharedPrefs.getInt(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY, 90000).toLong())
                    CommonUtils.updateBatteryStats(context!!)
                } else {
                    mainActivity.stopBatterySyncJob()
                }
                Utils.updateWatchPrefs(context!!)
                WatchBatteryWidget.updateWidget(context!!)
                false
            }
            PreferenceKey.BATTERY_SYNC_INTERVAL_KEY -> {
                mainActivity.createBatterySyncJob((newValue as Int).toLong())
                true
            }
            PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY -> {
                preference.sharedPreferences.edit().putBoolean(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY, newValue!! == true).apply()
                Utils.updateWatchPrefs(context!!)
                true
            }
            PreferenceKey.DND_SYNC_ENABLED_KEY -> {
                if (newValue!! == true) {
                    //TODO Actually check if watch has correct permissions
                    val command = getString(R.string.dnd_sync_adb_command)
                    AlertDialog.Builder(context!!)
                            .setTitle(R.string.dnd_sync_adb_dialog_title)
                            .setMessage(Compat.htmlFormat(String.format(getString(R.string.dnd_sync_adb_dialog_message), command)))
                            .setPositiveButton(R.string.dialog_button_done) { dialog, _ ->
                                preference.sharedPreferences.edit().putBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, true)
                                (preference as SwitchPreference).isChecked = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    activity?.startForegroundService(Intent(context, DnDHandler::class.java))
                                } else {
                                    activity?.startService(Intent(context, DnDHandler::class.java))
                                }
                                Utils.updateWatchPrefs(context!!)
                                dialog.cancel()
                            }
                            .setNegativeButton(R.string.dialog_button_cancel) { dialog, _ ->
                                preference.sharedPreferences.edit().putBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, false)
                                (preference as SwitchPreference).isChecked = false
                                Utils.updateWatchPrefs(context!!)
                                dialog.cancel()
                            }
                            .setNeutralButton(R.string.dialog_button_share) { dialog, _ ->
                                preference.sharedPreferences.edit().putBoolean(PreferenceKey.DND_SYNC_ENABLED_KEY, false)
                                (preference as SwitchPreference).isChecked = false
                                Utils.updateWatchPrefs(context!!)
                                Utils.shareText(context!!, command)
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
                Utils.updateWatchPrefs(context!!)
                false
            }
            PreferenceKey.DND_SYNC_RECEIVE_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                dndSyncWatchToPhonePref.isChecked = value
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted) {
                    Utils.updateWatchPrefs(context!!)
                } else {
                    Toast.makeText(context, getString(R.string.request_noti_policy_access_message), Toast.LENGTH_SHORT).show()
                    startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 12345)
                }
                false
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        mainActivity = activity as MainActivity

        addPreferencesFromResource(R.xml.prefs_general)
        setupGeneralPrefs()
        updateNotiSettingStatus()

        addPreferencesFromResource(R.xml.prefs_lock_phone)
        setupPhoneLockPrefs()

        addPreferencesFromResource(R.xml.prefs_battery_sync)
        setupBatterySyncPrefs()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addPreferencesFromResource(R.xml.prefs_dnd_sync)
            setupDnDPrefs()
        }

        addPreferencesFromResource(R.xml.prefs_about)
        setupAboutPrefs()
    }

    private fun setupGeneralPrefs() {
        val hideAppIconPref = findPreference(PreferenceKey.HIDE_APP_ICON_KEY)
        hideAppIconPref.onPreferenceChangeListener = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notiSettingsPref = findPreference(PreferenceKey.NOTIFICATION_SETTINGS_KEY)
            notiSettingsPref.onPreferenceClickListener = this
        }
    }

    private fun setupPhoneLockPrefs() {
        lockPhoneEnabledPref = findPreference(PreferenceKey.LOCK_PHONE_ENABLED) as SwitchPreference
        lockPhoneEnabledPref.onPreferenceChangeListener = this
    }

    private fun setupBatterySyncPrefs() {
        val batterySyncEnabledPref = findPreference(PreferenceKey.BATTERY_SYNC_ENABLED_KEY) as SwitchPreference
        batterySyncEnabledPref.onPreferenceChangeListener = this

        val batterySyncIntervalPref = findPreference(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY)
        batterySyncIntervalPref.onPreferenceChangeListener = this

        val batterySyncForcePref = findPreference(PreferenceKey.BATTERY_SYNC_NOW_KEY) as Preference
        batterySyncForcePref.onPreferenceClickListener = this

        val batterySyncPhoneChargedNotiPref = findPreference(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY)
        batterySyncPhoneChargedNotiPref.onPreferenceChangeListener = this
    }

    private fun setupDnDPrefs() {
        val dndSyncEnabledPref = findPreference(PreferenceKey.DND_SYNC_ENABLED_KEY) as SwitchPreference
        dndSyncEnabledPref.onPreferenceChangeListener = this
        dndSyncEnabledPref.onPreferenceClickListener = this

        dndSyncPhoneToWatchPref = findPreference(PreferenceKey.DND_SYNC_SEND_KEY) as CheckBoxPreference
        dndSyncPhoneToWatchPref.onPreferenceChangeListener = this

        dndSyncWatchToPhonePref = findPreference(PreferenceKey.DND_SYNC_RECEIVE_KEY) as CheckBoxPreference
        dndSyncWatchToPhonePref.onPreferenceChangeListener = this
    }

    private fun setupAboutPrefs() {
        val donatePref = findPreference("pref_donate") as Preference
        donatePref.onPreferenceClickListener = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 12345) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted) {
                Utils.updateWatchPrefs(context!!)
            } else {
                sharedPrefs.edit().putBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, false).apply()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isGrantingAdminPerms) {
            val isAdmin = mainActivity.isDeviceAdmin()
            sharedPrefs.edit().putBoolean(PreferenceKey.LOCK_PHONE_ENABLED, isAdmin).apply()
            lockPhoneEnabledPref.isChecked = isAdmin
            isGrantingAdminPerms = false
            Utils.updateWatchPrefs(context!!)
        }
        updateNotiSettingStatus()
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        when (preference) {
            is SeekbarDialogPreference -> {
                val frag = SeekbarDialogPrefFragment.newInstance(preference.key)
                frag.setTargetFragment(this, 0)
                frag.show(fragmentManager!!, "SeekbarDialogPrefFragment")
            }
            is ConfirmationDialogPreference -> {
                val frag = ConfirmationDialogPrefFragment.newInstance(preference.key)
                frag.setTargetFragment(this, 0)
                frag.show(fragmentManager!!, "ConfirmationDialogPrefFragment")
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun updateNotiSettingStatus() {
        val notisEnabled = NotificationManagerCompat.from(context!!).areNotificationsEnabled()
        if (notisEnabled) {
            notiSettingsPref.icon = context?.getDrawable(R.drawable.ic_notifications)
            notiSettingsPref.summary = getString(R.string.pref_noti_settings_summary_enabled)
        } else {
            notiSettingsPref.icon = context?.getDrawable(R.drawable.ic_notifications_off)
            notiSettingsPref.summary = getString(R.string.pref_noti_settings_summary_disabled)
        }
    }
}