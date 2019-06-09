/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.receiver.DeviceAdminChangeReceiver.Companion.DEVICE_ADMIN_ENABLED_KEY
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncLayer
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPrefFragment
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPreference
import com.boswelja.devicemanager.ui.appmanager.AppManagerActivity
import com.boswelja.devicemanager.ui.batterysync.BatterySyncPreferenceActivity
import com.boswelja.devicemanager.ui.donate.DonationDialogFragment
import com.boswelja.devicemanager.ui.interruptfiltersync.InterruptFilterSyncPreferenceActivity
import com.boswelja.devicemanager.ui.version.ChangelogDialogFragment

class SettingsFragment :
        PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private lateinit var preferenceSyncLayer: PreferenceSyncLayer

    private lateinit var phoneLockPreference: SwitchPreference

    private lateinit var openNotiSettingsPreference: Preference
    private lateinit var daynightModePreference: ListPreference

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
            DAYNIGHT_MODE_KEY -> activity?.recreate()
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            OPEN_BATTERY_SYNC_PREF_KEY -> {
                Intent(context!!, BatterySyncPreferenceActivity::class.java).also {
                    startActivity(it)
                }
                true
            }
            OPEN_INTERRUPT_FILTER_SYNC_PREF_KEY -> {
                Intent(context!!, InterruptFilterSyncPreferenceActivity::class.java).also {
                    startActivity(it)
                }
                true
            }
            OPEN_APP_MANAGER_KEY -> {
                Intent(context!!, AppManagerActivity::class.java).also {
                    startActivity(it)
                }
                true
            }
            OPEN_NOTI_SETTINGS_KEY -> {
                Intent().apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName!!)
                    } else {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", context?.packageName!!)
                        putExtra("app_uid", context?.applicationInfo?.uid!!)
                    }
                }.also { startActivity(it) }
                true
            }
            LEAVE_REVIEW_KEY -> {
                Intent(Intent.ACTION_VIEW).apply {
                    data = "https://play.google.com/store/apps/details?id=${context?.packageName}".toUri()
                    setPackage("com.android.vending")
                }.also { startActivity(it) }
                true
            }
            OPEN_DONATE_DIALOG_KEY -> {
                DonationDialogFragment().show(activity?.supportFragmentManager!!, "DonationDialog")
                true
            }
            VERSION_KEY -> {
                ChangelogDialogFragment().show(activity?.supportFragmentManager!!, "ChangelogDialog")
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key) {
            PHONE_LOCKING_ENABLED_KEY -> {
                val sharedPreferences = preference.sharedPreferences
                val value = newValue == true
                if (!Utils.isDeviceAdminEnabled(context!!)) {
                    AlertDialog.Builder(context!!)
                            .setTitle(R.string.device_admin_perm_grant_dialog_title)
                            .setMessage(R.string.device_admin_perm_grant_dialog_message)
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
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        return super.onCreateRecyclerView(inflater, parent, savedInstanceState).apply {
            isNestedScrollingEnabled = true
        }
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
                summary = getString(R.string.pref_noti_status_summary_enabled)
                setIcon(R.drawable.pref_ic_notifications_enabled)
            }
        } else {
            openNotiSettingsPreference.apply {
                summary = getString(R.string.pref_noti_status_summary_disabled)
                setIcon(R.drawable.pref_ic_notifications_disabled)
            }
        }

        daynightModePreference.summary = daynightModePreference.entry
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
        findPreference<Preference>(OPEN_APP_MANAGER_KEY)!!.onPreferenceClickListener = this

        addPreferencesFromResource(R.xml.prefs_lock_phone)
        phoneLockPreference = findPreference(PHONE_LOCKING_ENABLED_KEY)!!
        phoneLockPreference.onPreferenceChangeListener = this

        addPreferencesFromResource(R.xml.prefs_app_settings)
        openNotiSettingsPreference = findPreference(OPEN_NOTI_SETTINGS_KEY)!!
        openNotiSettingsPreference.onPreferenceClickListener = this
        daynightModePreference = findPreference(DAYNIGHT_MODE_KEY)!!

        addPreferencesFromResource(R.xml.prefs_about)
        findPreference<Preference>(LEAVE_REVIEW_KEY)!!.apply {
            isEnabled = !BuildConfig.DEBUG
            onPreferenceClickListener = this@SettingsFragment
        }
        findPreference<Preference>(OPEN_DONATE_DIALOG_KEY)!!.apply {
            isEnabled = !BuildConfig.DEBUG
            onPreferenceClickListener = this@SettingsFragment
        }
        findPreference<Preference>(VERSION_KEY)!!.apply {
            onPreferenceClickListener = this@SettingsFragment
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        when (preference) {
            is ConfirmationDialogPreference -> {
                ConfirmationDialogPrefFragment.newInstance(preference.key).apply {
                    setTargetFragment(this@SettingsFragment, 0)
                    show(this@SettingsFragment.fragmentManager!!, "ConfirmationDialogPrefFragment")
                }
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {
        const val OPEN_BATTERY_SYNC_PREF_KEY = "show_battery_sync_prefs"
        const val OPEN_INTERRUPT_FILTER_SYNC_PREF_KEY = "show_interrupt_filter_sync_prefs"
        const val OPEN_APP_MANAGER_KEY = "show_app_manager"

        const val OPEN_NOTI_SETTINGS_KEY = "show_noti_settings"
        const val DAYNIGHT_MODE_KEY = "daynight_mode"

        const val LEAVE_REVIEW_KEY = "review"
        const val OPEN_DONATE_DIALOG_KEY = "show_donate_dialog"
        const val VERSION_KEY = "version"
    }
}
