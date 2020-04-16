/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.phonelocking

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.phonelocking.Utils
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PhoneLockingPreferenceFragment :
        BasePreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    private val coroutineScope = MainScope()

    private lateinit var phoneLockModePreference: DropDownPreference
    private lateinit var openDeviceSettingsPreference: Preference
    private lateinit var phoneLockPreference: SwitchPreference

    private var snackbar: Snackbar? = null
    private var isEnablingPhoneLocking = false

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PHONE_LOCKING_ENABLED_KEY -> {
                val phoneLockingEnabled = sharedPreferences!!.getBoolean(key, false)
                phoneLockPreference.isChecked = phoneLockingEnabled
                if (phoneLockingEnabled) {
                    snackbar?.dismiss()
                }
                coroutineScope.launch(Dispatchers.IO) {
                    (activity as PhoneLockingPreferenceActivity).watchConnectionManager
                            ?.updatePreferenceOnWatch(key)
                }
            }
            PHONE_LOCKING_MODE_KEY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    updatePhoneLockModePrefSummary()
                    updateOpenDeviceSettingsTitle()
                    if (sharedPreferences!!.getBoolean(PHONE_LOCKING_ENABLED_KEY, false)) {
                        sharedPreferences.edit {
                            putBoolean(PHONE_LOCKING_ENABLED_KEY, false)
                        }
                        snackbar = Snackbar.make(view!!, R.string.phone_locking_disabled_by_mode_change, Snackbar.LENGTH_INDEFINITE)
                        snackbar!!.show()
                    }
                    when (sharedPreferences.getString(key, "0")) {
                        PHONE_LOCKING_MODE_DEVICE_ADMIN -> {
                            Utils.switchToDeviceAdminMode(context!!)
                        }
                        PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE -> {
                            Utils.switchToAccessibilityServiceMode(context!!)
                        }
                    }
                }
            }
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            OPEN_DEVICE_SETTINGS_KEY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                        sharedPreferences.getString(PHONE_LOCKING_MODE_KEY, "0")
                        == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
                    Utils.launchAccessibilitySettings(context!!)
                } else {
                    startActivity(Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")))
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                        sharedPreferences.getString(PHONE_LOCKING_MODE_KEY, "0")
                        == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
                    if (!Utils.isAccessibilityServiceEnabled(context!!)) {
                        isEnablingPhoneLocking = true
                        AlertDialog.Builder(context!!)
                                .setTitle(R.string.dialog_phone_locking_accessibility_service_title)
                                .setMessage(R.string.dialog_phone_locking_accessibility_service_desc)
                                .setPositiveButton(R.string.dialog_button_enable) { _, _ ->
                                    Utils.launchAccessibilitySettings(context!!)
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
                        coroutineScope.launch(Dispatchers.IO) {
                            getWatchConnectionManager()?.updatePreferenceOnWatch(key)
                        }
                    }
                } else {
                    if (!Utils.isDeviceAdminEnabled(context!!)) {
                        isEnablingPhoneLocking = true
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
                        coroutineScope.launch(Dispatchers.IO) {
                            getWatchConnectionManager()?.updatePreferenceOnWatch(key)
                        }
                    }
                }
                false
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_phone_locking)

        phoneLockPreference = findPreference(PHONE_LOCKING_ENABLED_KEY)!!
        phoneLockPreference.onPreferenceChangeListener = this

        phoneLockModePreference = findPreference(PHONE_LOCKING_MODE_KEY)!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            updatePhoneLockModePrefSummary()
        } else {
            phoneLockModePreference.isEnabled = false
            phoneLockModePreference.summary = getString(R.string.pref_phone_locking_mode_no_option_summary)
        }

        openDeviceSettingsPreference = findPreference(OPEN_DEVICE_SETTINGS_KEY)!!
        openDeviceSettingsPreference.onPreferenceClickListener = this
        updateOpenDeviceSettingsTitle()
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (isEnablingPhoneLocking) {
            val enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    sharedPreferences.getString(PHONE_LOCKING_MODE_KEY, "0")
                    == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
                Utils.isAccessibilityServiceEnabled(context!!)
            } else {
                Utils.isDeviceAdminEnabled(context!!)
            }
            sharedPreferences.edit {
                putBoolean(PHONE_LOCKING_ENABLED_KEY, enabled)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun updatePhoneLockModePrefSummary() {
        phoneLockModePreference.summary = phoneLockModePreference.entry
    }

    private fun updateOpenDeviceSettingsTitle() {
        openDeviceSettingsPreference.title = if (phoneLockModePreference.value == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
            getString(R.string.pref_phone_locking_accessibility_settings_title)
        } else {
            getString(R.string.pref_phone_locking_admin_settings_title)
        }
    }

    companion object {
        const val PHONE_LOCKING_MODE_KEY = "phone_locking_mode"
        const val PHONE_LOCKING_MODE_DEVICE_ADMIN = "0"
        const val PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE = "1"

        private const val OPEN_DEVICE_SETTINGS_KEY = "open_device_settings"
    }
}
