/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.DnDLocalChangeListener
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncLayer
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPrefFragment
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPreference
import com.boswelja.devicemanager.preference.seekbardialog.SeekbarDialogPrefFragment
import com.boswelja.devicemanager.preference.seekbardialog.SeekbarDialogPreference
import com.boswelja.devicemanager.widget.WatchBatteryWidget
import com.google.android.material.snackbar.Snackbar

class SettingsFragment :
        PreferenceFragmentCompat(),
        Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var preferenceSyncLayer: PreferenceSyncLayer

    private lateinit var batterySyncEnabledPref: SwitchPreference
    private lateinit var batteryPhoneChargedNotiPref: CheckBoxPreference
    private lateinit var batteryWatchChargedNotiPref: CheckBoxPreference

    private lateinit var notiSettingsPref: Preference
    private lateinit var batteryOptPref: ConfirmationDialogPreference
    private lateinit var lockPhoneEnabledPref: SwitchPreference
    private lateinit var dndSyncPhoneToWatchPref: SwitchPreference
    private lateinit var dndSyncWatchToPhonePref: SwitchPreference
    private lateinit var dndSyncWithTheaterModePref: SwitchPreference

    private lateinit var mainActivity: MainActivity
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    private var isGrantingAdminPerms = false

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceKey.LOCK_PHONE_ENABLED -> {
                lockPhoneEnabledPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                batterySyncEnabledPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY -> {
                batteryPhoneChargedNotiPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY -> {
                batteryWatchChargedNotiPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY -> {
                dndSyncPhoneToWatchPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY -> {
                dndSyncWatchToPhonePref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY -> {
                dndSyncWithTheaterModePref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
        }
    }

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
            PreferenceKey.DONATE_KEY -> {
                DonationDialogFragment().show(activity?.supportFragmentManager!!, "DonationDialog")
                true
            }
            PreferenceKey.DAYNIGHT_SWITCH_KEY -> {
                Utils.switchDayNightMode(mainActivity)
                true
            }
            else -> false
        }
    }

    @SuppressLint("BatteryLife")
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key) {
            PreferenceKey.HIDE_APP_ICON_KEY -> {
                mainActivity.changeAppIconVisibility(newValue == true)
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
                                sharedPrefs.edit()
                                        .putBoolean(preference.key, false)
                                        .apply()
                            }
                            .show()
                } else {
                    sharedPrefs.edit()
                            .putBoolean(preference.key, value)
                            .apply()
                    preferenceSyncLayer.updateData()
                }
                false
            }
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                val value = newValue == true
                sharedPrefs.edit().putBoolean(preference.key, value).apply()
                if (value) {
                    Utils.createBatterySyncJob(context!!)
                    CommonUtils.updateBatteryStats(context!!)
                } else {
                    Utils.stopBatterySyncJob(context!!)
                }
                preferenceSyncLayer.updateData()
                WatchBatteryWidget.updateWidget(context!!)
                false
            }
            PreferenceKey.BATTERY_SYNC_INTERVAL_KEY -> {
                Utils.createBatterySyncJob(context!!, newValue as Long)
                true
            }
            PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                preferenceSyncLayer.updateData()
                false
            }
            PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                preferenceSyncLayer.updateData()
                false
            }
            PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY -> {
                val value = newValue == true
                if (value) {
                    val dndDialog = DnDSyncDialogFragment()
                    dndDialog.show(mainActivity.supportFragmentManager, "DnDSyncDialogFragment")
                    dndDialog.setResponseListener(object : DnDSyncDialogFragment.ResponseListener {
                        override fun onResponse(success: Boolean) {
                            preference.sharedPreferences.edit().putBoolean(preference.key, success).apply()
                            preferenceSyncLayer.updateData()
                            if (success) {
                                Compat.startService(context!!, Intent(context!!, DnDLocalChangeListener::class.java))
                            }
                        }
                    })
                } else {
                    preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                    preferenceSyncLayer.updateData()
                    context?.stopService(Intent(context!!, DnDLocalChangeListener::class.java))
                }
                false
            }
            PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                if (value) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted) {
                        preferenceSyncLayer.updateData()
                    } else {
                        Toast.makeText(context, getString(R.string.request_noti_policy_access_message), Toast.LENGTH_SHORT).show()
                        startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 12345)
                    }
                } else {
                    preferenceSyncLayer.updateData()
                }
                false
            }
            PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(preference.key, value).apply()
                if (value) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted) {
                        preferenceSyncLayer.updateData()
                    } else {
                        Toast.makeText(context, getString(R.string.request_noti_policy_access_message), Toast.LENGTH_SHORT).show()
                        startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 54321)
                    }
                } else {
                    preferenceSyncLayer.updateData()
                }
                false
            }
            PreferenceKey.BATTERY_OPT_KEY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && newValue == true) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:${context?.packageName}")
                    startActivity(intent)
                }
                true
            }
            else -> true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceSyncLayer = PreferenceSyncLayer(context!!)

        notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mainActivity = activity as MainActivity
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context!!)

        addPreferencesFromResource(R.xml.prefs_battery_sync)
        setupBatterySyncPrefs()

        addPreferencesFromResource(R.xml.prefs_dnd_sync)
        setupDnDPrefs()

        addPreferencesFromResource(R.xml.prefs_lock_phone)
        setupPhoneLockPrefs()

        addPreferencesFromResource(R.xml.prefs_general)
        setupGeneralPrefs()

        addPreferencesFromResource(R.xml.prefs_about)
        setupAboutPrefs()

        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    private fun setupGeneralPrefs() {
        val hideAppIconPref = findPreference<Preference>(PreferenceKey.HIDE_APP_ICON_KEY)!!
        hideAppIconPref.onPreferenceChangeListener = this

        notiSettingsPref = findPreference(PreferenceKey.NOTIFICATION_SETTINGS_KEY)!!
        notiSettingsPref.onPreferenceClickListener = this
        updateNotiSettingSummary()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            batteryOptPref = findPreference(PreferenceKey.BATTERY_OPT_KEY)!!
            batteryOptPref.onPreferenceChangeListener = this
        }

        val dayNightSwitchPref = findPreference<Preference>(PreferenceKey.DAYNIGHT_SWITCH_KEY)!!
        dayNightSwitchPref.onPreferenceClickListener = this
    }

    private fun setupPhoneLockPrefs() {
        lockPhoneEnabledPref = findPreference(PreferenceKey.LOCK_PHONE_ENABLED)!!
        lockPhoneEnabledPref.onPreferenceChangeListener = this
    }

    private fun setupBatterySyncPrefs() {
        batterySyncEnabledPref = findPreference(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)!!
        batterySyncEnabledPref.onPreferenceChangeListener = this

        val batterySyncIntervalPref = findPreference<SeekbarDialogPreference>(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY)!!
        batterySyncIntervalPref.onPreferenceChangeListener = this

        val batterySyncForcePref = findPreference<Preference>(PreferenceKey.BATTERY_SYNC_NOW_KEY)!!
        batterySyncForcePref.onPreferenceClickListener = this

        batteryPhoneChargedNotiPref = findPreference(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY)!!
        batteryPhoneChargedNotiPref.onPreferenceChangeListener = this

        batteryWatchChargedNotiPref = findPreference(PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY)!!
        batteryWatchChargedNotiPref.onPreferenceChangeListener = this
    }

    private fun setupDnDPrefs() {
        dndSyncPhoneToWatchPref = findPreference(PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY)!!
        dndSyncPhoneToWatchPref.onPreferenceChangeListener = this

        dndSyncWatchToPhonePref = findPreference(PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY)!!
        dndSyncWatchToPhonePref.onPreferenceChangeListener = this

        dndSyncWithTheaterModePref = findPreference(PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY)!!
        dndSyncWithTheaterModePref.onPreferenceChangeListener = this
    }

    private fun setupAboutPrefs() {
        val donatePref = findPreference<Preference>(PreferenceKey.DONATE_KEY)!!
        donatePref.onPreferenceClickListener = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            12345 or 54321-> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted) {
                    preferenceSyncLayer.updateData()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isGrantingAdminPerms) {
            val isAdmin = mainActivity.isDeviceAdmin()
            sharedPrefs.edit().putBoolean(PreferenceKey.LOCK_PHONE_ENABLED, isAdmin).apply()
            isGrantingAdminPerms = false
            preferenceSyncLayer.updateData()
        }
        updateNotiSettingSummary()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pwm = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringBattery = pwm.isIgnoringBatteryOptimizations(context?.packageName)
            batteryOptPref.setValue(isIgnoringBattery)
            updateBatteryOptSummary(isIgnoringBattery)
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                sharedPrefs.edit().putBoolean(PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY, false).apply()
                dndSyncWatchToPhonePref.isChecked = false
                sharedPrefs.edit().putBoolean(PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY, false).apply()
                dndSyncWithTheaterModePref.isChecked = false
                preferenceSyncLayer.updateData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
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

    private fun updateNotiSettingSummary() {
        val notisEnabled = NotificationManagerCompat.from(context!!).areNotificationsEnabled()
        if (notisEnabled) {
            notiSettingsPref.icon = context?.getDrawable(R.drawable.ic_notifications)
            notiSettingsPref.summary = getString(R.string.pref_noti_settings_summary_enabled)
        } else {
            notiSettingsPref.icon = context?.getDrawable(R.drawable.ic_notifications_off)
            notiSettingsPref.summary = getString(R.string.pref_noti_settings_summary_disabled)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateBatteryOptSummary(value: Boolean) {
        val summaryRes = if (value) {
            R.string.pref_battery_opt_summary_disabled
        } else {
            R.string.pref_battery_opt_summary_enabled
        }
        val iconRes = if (value) {
            R.drawable.ic_check
        } else {
            R.drawable.ic_warning
        }
        batteryOptPref.setSummary(summaryRes)
        batteryOptPref.setIcon(iconRes)
    }
}