package com.boswelja.devicemanager.phonelocking.ui

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.common.ui.activity.BasePreferenceFragment
import com.boswelja.devicemanager.phonelocking.DeviceAdminChangeReceiver
import com.boswelja.devicemanager.phonelocking.Utils
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingSettingsViewModel.Companion.PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingSettingsViewModel.Companion.PHONE_LOCKING_MODE_DEVICE_ADMIN
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingSettingsViewModel.Companion.PHONE_LOCKING_MODE_KEY
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class PhoneLockingPreferenceFragment :
    BasePreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    Preference.OnPreferenceClickListener,
    Preference.OnPreferenceChangeListener {

    private val viewModel: PhoneLockingSettingsViewModel by viewModels()

    private lateinit var phoneLockModePreference: DropDownPreference
    private lateinit var openDeviceSettingsPreference: Preference
    private lateinit var phoneLockPreference: SwitchPreference

    private var snackbar: Snackbar? = null
    private var isEnablingPhoneLocking = false

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PHONE_LOCKING_ENABLED_KEY -> {
                val phoneLockingEnabled = sharedPreferences!!.getBoolean(key, false)
                setPhoneLockingEnabled(phoneLockingEnabled)
            }
            PHONE_LOCKING_MODE_KEY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val phoneLockingMode = sharedPreferences?.getString(PHONE_LOCKING_MODE_KEY, "0")
                    setPhoneLockingMode(phoneLockingMode)
                }
            }
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            OPEN_DEVICE_SETTINGS_KEY -> {
                openDeviceSettings()
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key) {
            PHONE_LOCKING_ENABLED_KEY -> {
                tryTogglePhoneLocking()
                false
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeConnectedWatchId()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_phone_locking)

        phoneLockPreference = findPreference(PHONE_LOCKING_ENABLED_KEY)!!
        phoneLockPreference.onPreferenceChangeListener = this

        phoneLockModePreference = findPreference(PHONE_LOCKING_MODE_KEY)!!
        updatePhoneLockModePrefSummary()

        openDeviceSettingsPreference = findPreference(OPEN_DEVICE_SETTINGS_KEY)!!
        openDeviceSettingsPreference.onPreferenceClickListener = this
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart() called")
        updateOpenDeviceSettingsTitle()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (isEnablingPhoneLocking && canEnablePhoneLocking()) {
            setPhoneLockingEnabled(true)
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop() called")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Checks whether phone locking can be enabled, i.e. the required services are enabled.
     * @return true if phone locking can be enabled, false otherwise.
     */
    @SuppressLint("NewApi")
    private fun canEnablePhoneLocking(): Boolean {
        return if (isInAccessibilityMode()) {
            Utils.isAccessibilityServiceEnabled(requireContext())
        } else {
            Utils.isDeviceAdminEnabled(requireContext())
        }
    }

    /**
     * Sets whether phone locking is enabled, and updates the connected watch.
     * @param enabled true if phone locking should be enabled, false otherwise.
     */
    private fun setPhoneLockingEnabled(enabled: Boolean) {
        Timber.d("setPhoneLockingEnabled($enabled) called")
        phoneLockPreference.isChecked = enabled
        if (enabled) snackbar?.dismiss()
        viewModel.setPhoneLockingEnabled(enabled)
    }

    /**
     * Update [phoneLockModePreference] summary. If phone locking mode isn't an option on the
     * device, it will be disabled and a message will be shown.
     */
    private fun updatePhoneLockModePrefSummary() {
        Timber.d("updatePhoneLockModePrefSummary() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Timber.i("Updating phone locking mode summary")
            phoneLockModePreference.summary = phoneLockModePreference.entry
        } else {
            Timber.i("Phone locking mode not available, disabling")
            phoneLockModePreference.isEnabled = false
            phoneLockModePreference.summary =
                getString(R.string.pref_phone_locking_mode_no_option_summary)
        }
    }

    /**
     * Check if phone locking mode is [PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE].
     * @return true if phone locking is in accessibility mode, false otherwise.
     */
    private fun isInAccessibilityMode(): Boolean =
        viewModel.phoneLockingMode.value == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE

    /**
     * Opens the device settings that correspond to phone locking mode. If phone locking mode is
     * [PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE], this launches accessibility settings, otherwise
     * it launches device administrator settings.
     */
    private fun openDeviceSettings() {
        Timber.d("openDeviceSettings() called")
        if (isInAccessibilityMode()) {
            Timber.i("Opening accessibility settings")
            requireContext().startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } else {
            Timber.i("Opening device admin settings")
            Intent()
                .apply {
                    ComponentName(
                        "com.android.settings", "com.android.settings.DeviceAdminSettings"
                    )
                        .also { component = it }
                }
                .also { startActivity(it) }
        }
    }

    /**
     * Try to toggle phone locking. If [canEnablePhoneLocking] is false, this will ask the user to
     * enable the required services first.
     */
    private fun tryTogglePhoneLocking() {
        Timber.d("tryTogglePhoneLocking() called")
        if (!canEnablePhoneLocking()) {
            isEnablingPhoneLocking = true
            AlertDialog.Builder(requireContext())
                .apply {
                    setNegativeButton(R.string.dialog_button_cancel) { _, _ ->
                        Timber.i("User declined, aborting")
                        setPhoneLockingEnabled(false)
                    }
                    if (isInAccessibilityMode()) {
                        setTitle(R.string.dialog_phone_locking_accessibility_service_title)
                        setMessage(R.string.dialog_phone_locking_accessibility_service_desc)
                        setPositiveButton(R.string.dialog_button_enable) { _, _ ->
                            Timber.i("Opening accessibility settings")
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    } else {
                        setTitle(R.string.device_admin_perm_grant_dialog_title)
                        setMessage(R.string.device_admin_perm_grant_dialog_message)
                        setPositiveButton(R.string.dialog_button_grant) { _, _ ->
                            Timber.i("Opening device admin request")
                            requestDeviceAdminPerms()
                        }
                    }
                }
                .also { it.show() }
        } else {
            Timber.i("Disabling phone locking")
            setPhoneLockingEnabled(!sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false))
        }
    }

    /**
     * Sets the phone locking mode.
     * @param newMode The new phone locking mode to set. Should be either
     * [PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE], or [PHONE_LOCKING_MODE_DEVICE_ADMIN].
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun setPhoneLockingMode(newMode: String?) {
        Timber.d("setPhoneLockingMode($newMode) called")
        updatePhoneLockModePrefSummary()
        updateOpenDeviceSettingsTitle()
        if (sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false)) {
            Timber.i("Disabling phone locking and notifying user")
            sharedPreferences.edit { putBoolean(PHONE_LOCKING_ENABLED_KEY, false) }
            snackbar =
                Snackbar.make(
                    requireView(),
                    R.string.phone_locking_disabled_by_mode_change,
                    Snackbar.LENGTH_INDEFINITE
                )
            snackbar!!.show()
        }
        when (newMode) {
            PHONE_LOCKING_MODE_DEVICE_ADMIN -> {
                Timber.i("Switching to device admin mode")
                viewModel.switchToDeviceAdminMode()
            }
            PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE -> {
                Timber.i("Switching to accessibility service mode")
                viewModel.switchToAccessibilityServiceMode()
            }
            else -> Timber.w("Unknown or invalid phone locking mode")
        }
    }

    /** Updates [openDeviceSettingsPreference] title to match the current phone locking mode. */
    private fun updateOpenDeviceSettingsTitle() {
        Timber.d("updateOpenDeviceSettingsTitle() called")
        openDeviceSettingsPreference.title =
            if (isInAccessibilityMode()) {
                getString(R.string.pref_phone_locking_accessibility_settings_title)
            } else {
                getString(R.string.pref_phone_locking_admin_settings_title)
            }
    }

    /**
     * Request Device Administrator permissions for Wearable Extensions.
     * @param context [Context].
     */
    private fun requestDeviceAdminPerms() {
        val intent =
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    DeviceAdminChangeReceiver().getWho(requireContext())
                )
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.device_admin_desc)
                )
            }
        startActivity(intent)
    }

    companion object {
        private const val OPEN_DEVICE_SETTINGS_KEY = "open_device_settings"
    }
}
