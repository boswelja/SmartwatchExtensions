/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.extensions

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPrefFragment
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPreference
import com.boswelja.devicemanager.receiver.DeviceAdminChangeReceiver.Companion.DEVICE_ADMIN_ENABLED_KEY
import com.boswelja.devicemanager.ui.appmanager.AppManagerActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.ui.batterysync.BatterySyncPreferenceActivity
import com.boswelja.devicemanager.ui.interruptfiltersync.InterruptFilterSyncPreferenceActivity

class ExtensionsFragment :
        BasePreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private lateinit var phoneLockPreference: SwitchPreference

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
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (val key = preference?.key) {
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
                                        .putBoolean(key, false)
                                        .apply()
                            }
                            .show()
                } else {
                    sharedPreferences.edit()
                            .putBoolean(key, value)
                            .apply()
                    getWatchConnectionManager()?.updatePreferenceOnWatch(key)
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

    override fun onResume() {
        super.onResume()
        val isDeviceAdminEnabled = Utils.isDeviceAdminEnabled(context!!)
        if (!isDeviceAdminEnabled) {
            preferenceManager.sharedPreferences.edit()
                    .putBoolean(PHONE_LOCKING_ENABLED_KEY, false)
                    .apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_main)
        findPreference<Preference>(OPEN_BATTERY_SYNC_PREF_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(OPEN_INTERRUPT_FILTER_SYNC_PREF_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(OPEN_APP_MANAGER_KEY)!!.onPreferenceClickListener = this

        addPreferencesFromResource(R.xml.prefs_lock_phone)
        phoneLockPreference = findPreference(PHONE_LOCKING_ENABLED_KEY)!!
        phoneLockPreference.onPreferenceChangeListener = this
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        when (preference) {
            is ConfirmationDialogPreference -> {
                ConfirmationDialogPrefFragment.newInstance(preference.key).apply {
                    setTargetFragment(this@ExtensionsFragment, 0)
                    show(this@ExtensionsFragment.fragmentManager!!, "ConfirmationDialogPrefFragment")
                }
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {
        const val OPEN_BATTERY_SYNC_PREF_KEY = "show_battery_sync_prefs"
        const val OPEN_INTERRUPT_FILTER_SYNC_PREF_KEY = "show_interrupt_filter_sync_prefs"
        const val OPEN_APP_MANAGER_KEY = "show_app_manager"
    }
}
