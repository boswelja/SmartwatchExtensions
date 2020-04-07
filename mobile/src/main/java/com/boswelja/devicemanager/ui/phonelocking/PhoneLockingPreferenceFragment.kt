package com.boswelja.devicemanager.ui.phonelocking

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.receiver.DeviceAdminChangeReceiver
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PhoneLockingPreferenceFragment :
        BasePreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    private val coroutineScope = MainScope()

    private lateinit var phoneLockPreference: SwitchPreference

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DeviceAdminChangeReceiver.DEVICE_ADMIN_ENABLED_KEY -> {
                val isDeviceAdminEnabled = Utils.isDeviceAdminEnabled(context!!)
                sharedPreferences!!.edit()
                        .putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, isDeviceAdminEnabled)
                        .apply()
            }
            PreferenceKey.PHONE_LOCKING_ENABLED_KEY -> {
                phoneLockPreference.isChecked = sharedPreferences!!.getBoolean(key, false)
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (val key = preference?.key) {
            PreferenceKey.PHONE_LOCKING_ENABLED_KEY -> {
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
                    coroutineScope.launch {
                        getWatchConnectionManager()?.updatePreferenceOnWatch(key)
                    }
                }
                false
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_phone_locking)

        phoneLockPreference = findPreference(PreferenceKey.PHONE_LOCKING_ENABLED_KEY)!!
        phoneLockPreference.onPreferenceChangeListener = this
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val isDeviceAdminEnabled = Utils.isDeviceAdminEnabled(context!!)
        if (!isDeviceAdminEnabled) {
            sharedPreferences.edit {
                putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}