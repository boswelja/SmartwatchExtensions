package com.boswelja.devicemanager.phonelocking.ui

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.phonelocking.DeviceAdminChangeReceiver
import com.boswelja.devicemanager.phonelocking.PhoneLockingAccessibilityService
import com.boswelja.devicemanager.phonelocking.Utils
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PhoneLockingSettingsViewModel internal constructor(
    application: Application,
    private val dispatcher: CoroutineDispatcher,
    private val sharedPreferences: SharedPreferences,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    private val _phoneLockingEnabled = MutableLiveData(
        sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false)
    )
    private val _phoneLockingMode = MutableLiveData(
        sharedPreferences.getString(PHONE_LOCKING_MODE_KEY, PHONE_LOCKING_MODE_DEVICE_ADMIN)
            ?: PHONE_LOCKING_MODE_DEVICE_ADMIN
    )

    val phoneLockingEnabled: LiveData<Boolean>
        get() = _phoneLockingEnabled
    val phoneLockingMode: LiveData<String>
        get() = _phoneLockingMode

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Dispatchers.IO,
        PreferenceManager.getDefaultSharedPreferences(application),
        WatchManager.getInstance(application)
    )

    /**
     * Switch Phone Locking mode to Device Administrator. This disables Accessibility Service
     * components.
     */
    fun switchToDeviceAdminMode() {
        if (phoneLockingMode.value != PHONE_LOCKING_MODE_DEVICE_ADMIN) {
            val context = getApplication<Application>()
            Timber.i("Switching to Device Administrator mode")
            context.packageManager.apply {
                setComponentEnabledSetting(
                    ComponentName(context, PhoneLockingAccessibilityService::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                setComponentEnabledSetting(
                    ComponentName(context, DeviceAdminChangeReceiver::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            sharedPreferences.edit {
                putString(PHONE_LOCKING_MODE_KEY, PHONE_LOCKING_MODE_DEVICE_ADMIN)
                putBoolean(PHONE_LOCKING_ENABLED_KEY, false) // Disable phone locking automatically
            }
            _phoneLockingMode.postValue(PHONE_LOCKING_MODE_DEVICE_ADMIN)
            _phoneLockingEnabled.postValue(false)
        }
    }

    /**
     * Switch Phone Locking mode to Accessibility Service. This disables Device Administrator
     * components.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun switchToAccessibilityServiceMode() {
        if (phoneLockingMode.value != PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
            val context = getApplication<Application>()
            Timber.i("Switching to Accessibility Service mode")
            if (Utils.isDeviceAdminEnabled(context)) {
                sharedPreferences.edit {
                    putBoolean(DeviceAdminChangeReceiver.DEVICE_ADMIN_ENABLED_KEY, false)
                }
                context.getSystemService<DevicePolicyManager>()?.removeActiveAdmin(
                    ComponentName(context, DeviceAdminChangeReceiver::class.java)
                )
            }

            context.packageManager.apply {
                setComponentEnabledSetting(
                    ComponentName(context, PhoneLockingAccessibilityService::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                setComponentEnabledSetting(
                    ComponentName(context, DeviceAdminChangeReceiver::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            sharedPreferences.edit {
                putString(PHONE_LOCKING_MODE_KEY, PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE)
                putBoolean(PHONE_LOCKING_ENABLED_KEY, false) // Disable phone locking automatically
            }
            _phoneLockingMode.postValue(PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE)
            _phoneLockingEnabled.postValue(false)
        }
    }

    fun setPhoneLockingEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            sharedPreferences.edit(commit = true) {
                putBoolean(PHONE_LOCKING_ENABLED_KEY, isEnabled)
            }
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                PHONE_LOCKING_ENABLED_KEY,
                isEnabled
            )
        }
    }

    companion object {
        const val PHONE_LOCKING_MODE_KEY = "phone_locking_mode"
        const val PHONE_LOCKING_MODE_DEVICE_ADMIN = "0"
        const val PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE = "1"
    }
}
