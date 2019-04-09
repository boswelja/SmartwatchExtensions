/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.DeviceAdminChangeReceiver.Companion.DEVICE_ADMIN_ENABLED_KEY
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncLayer
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPrefFragment
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPreference
import com.boswelja.devicemanager.ui.batterysync.BatterySyncPreferenceActivity
import com.boswelja.devicemanager.ui.interruptfiltersync.InterruptFilterSyncPreferenceActivity

class SettingsFragment :
        PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private lateinit var preferenceSyncLayer: PreferenceSyncLayer

    private lateinit var phoneLockPreference: SwitchPreference

    private lateinit var openNotiSettingsPreference: Preference
    private lateinit var batteryOptimisationStatusPreference: ConfirmationDialogPreference

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DEVICE_ADMIN_ENABLED_KEY -> {
                val isDeviceAdminEnabled = Utils.isDeviceAdminEnabled(context!!)
                sharedPreferences!!.edit()
                        .putBoolean(PHONE_LOCKING_ENABLED_KEY, isDeviceAdminEnabled)
                        .apply()
            }
            PHONE_LOCKING_ENABLED_KEY -> {
                phoneLockPreference.isChecked = sharedPreferences!!.getBoolean(key, false)
            }
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        val key = preference?.key!!
        return when (key) {
            OPEN_BATTERY_SYNC_PREF_KEY -> {
                val intent = Intent(context!!, BatterySyncPreferenceActivity::class.java)
                context!!.startActivity(intent)
                true
            }
            OPEN_INTERRUPT_FILTER_SYNC_PREF_KEY -> {
                val intent = Intent(context!!, InterruptFilterSyncPreferenceActivity::class.java)
                context!!.startActivity(intent)
                true
            }
            OPEN_NOTI_SETTINGS_KEY -> {
                val intent = Intent()
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName!!)
                } else {
                    intent.putExtra("app_package", context?.packageName!!)
                    intent.putExtra("app_uid", context?.applicationInfo?.uid!!)
                }
                startActivity(intent)
                true
            }
            SWITCH_DAYNIGHT_MODE_KEY -> {
                Utils.switchDayNightMode(activity as MainActivity)
                true
            }
            OPEN_DONATE_DIALOG_KEY -> {
                DonationDialogFragment().show(activity?.supportFragmentManager!!, "DonationDialog")
                true
            }
            else -> false
        }
    }

    @SuppressLint("BatteryLife")
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        val key = preference?.key!!
        return when (key) {
            HIDE_APP_ICON_KEY -> {
                Utils.setAppLauncherIconVisibility(context!!, newValue == true)
                true
            }
            PHONE_LOCKING_ENABLED_KEY -> {
                val sharedPreferences = preference.sharedPreferences
                val value = newValue == true
                if (!Utils.isDeviceAdminEnabled(context!!)) {
                    AlertDialog.Builder(context!!)
                            .setTitle(R.string.grant_device_admin_perm_dialog_title)
                            .setMessage(R.string.grant_device_admin_perm_dialog_message)
                            .setPositiveButton(R.string.dialog_button_grant) { _, _ ->
                                Utils.requestDeviceAdminPerms(context!!)
                            }
                            .setNegativeButton(R.string.dialog_button_cancel) { _, _ ->
                                sharedPreferences.edit()
                                        .putBoolean(preference.key, false)
                                        .apply()
                            }
                            .show()
                } else {
                    sharedPreferences.edit()
                            .putBoolean(preference.key, value)
                            .apply()
                    preferenceSyncLayer.pushNewData()
                }
                false
            }
            BATTERY_OPTIMISATION_STATUS_KEY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && newValue == true) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:${context?.packageName}")
                    startActivity(intent)
                }
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        val isDeviceAdminEnabled = Utils.isDeviceAdminEnabled(context!!)
        if (!isDeviceAdminEnabled) {
            preferenceManager.sharedPreferences.edit()
                    .putBoolean(PHONE_LOCKING_ENABLED_KEY, false)
                    .apply()
        }

        val notificationsAllowed = NotificationManagerCompat.from(context!!).areNotificationsEnabled()
        if (notificationsAllowed) {
            openNotiSettingsPreference.apply {
                summary = getString(R.string.pref_noti_settings_summary_enabled)
                setIcon(R.drawable.pref_ic_notifications_enabled)
            }
        } else {
            openNotiSettingsPreference.apply {
                summary = getString(R.string.pref_noti_settings_summary_disabled)
                setIcon(R.drawable.pref_ic_notifications_disabled)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isIgnoringBatteryOptimisation = (context?.getSystemService(Context.POWER_SERVICE) as PowerManager)
                    .isIgnoringBatteryOptimizations(context?.packageName!!)
            if (isIgnoringBatteryOptimisation) {
                batteryOptimisationStatusPreference.apply {
                    summary = getString(R.string.pref_battery_opt_summary_disabled)
                    setIcon(R.drawable.ic_check)
                }
            } else {
                batteryOptimisationStatusPreference.apply {
                    summary = getString(R.string.pref_battery_opt_summary_enabled)
                    setIcon(R.drawable.pref_ic_warning)
                }
            }
            preferenceManager.sharedPreferences.edit()
                    .putBoolean(BATTERY_OPTIMISATION_STATUS_KEY, isIgnoringBatteryOptimisation)
                    .apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceSyncLayer = PreferenceSyncLayer(context!!)

        addPreferencesFromResource(R.xml.prefs_main)
        findPreference<Preference>(OPEN_BATTERY_SYNC_PREF_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(OPEN_INTERRUPT_FILTER_SYNC_PREF_KEY)!!.onPreferenceClickListener = this

        addPreferencesFromResource(R.xml.prefs_lock_phone)
        phoneLockPreference = findPreference(PHONE_LOCKING_ENABLED_KEY)!!
        phoneLockPreference.onPreferenceChangeListener = this

        addPreferencesFromResource(R.xml.prefs_general)
        findPreference<ConfirmationDialogPreference>(HIDE_APP_ICON_KEY)!!.onPreferenceChangeListener = this
        openNotiSettingsPreference = findPreference(OPEN_NOTI_SETTINGS_KEY)!!
        openNotiSettingsPreference.onPreferenceClickListener = this
        findPreference<Preference>(SWITCH_DAYNIGHT_MODE_KEY)!!.onPreferenceClickListener = this
        batteryOptimisationStatusPreference = findPreference(BATTERY_OPTIMISATION_STATUS_KEY)!!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            findPreference<PreferenceCategory>("general_category")!!.removePreference(batteryOptimisationStatusPreference)
        } else {
            batteryOptimisationStatusPreference.onPreferenceChangeListener = this
        }

        addPreferencesFromResource(R.xml.prefs_about)
        findPreference<Preference>(OPEN_DONATE_DIALOG_KEY)!!.onPreferenceClickListener = this
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        when (preference) {
            is ConfirmationDialogPreference -> {
                val frag = ConfirmationDialogPrefFragment.newInstance(preference.key)
                frag.setTargetFragment(this, 0)
                frag.show(fragmentManager!!, "ConfirmationDialogPrefFragment")
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {
        const val OPEN_BATTERY_SYNC_PREF_KEY = "show_battery_sync_prefs"
        const val OPEN_INTERRUPT_FILTER_SYNC_PREF_KEY = "show_interrupt_filter_sync_prefs"

        const val HIDE_APP_ICON_KEY = "hide_app_icon"
        const val OPEN_NOTI_SETTINGS_KEY = "show_noti_settings"
        const val SWITCH_DAYNIGHT_MODE_KEY = "daynight_switch"
        const val BATTERY_OPTIMISATION_STATUS_KEY = "battery_optimisation_status"

        const val OPEN_DONATE_DIALOG_KEY = "show_donate_dialog"
    }
}