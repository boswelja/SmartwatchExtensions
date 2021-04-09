package com.boswelja.devicemanager.phonelocking.ui

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appsettings.Settings
import com.boswelja.devicemanager.appsettings.appSettingsStore
import com.boswelja.devicemanager.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.phonelocking.DeviceAdminChangeReceiver
import com.boswelja.devicemanager.phonelocking.PhoneLockingAccessibilityService
import com.boswelja.devicemanager.phonelocking.Utils.isAccessibilityServiceEnabled
import com.boswelja.devicemanager.phonelocking.Utils.isDeviceAdminEnabled
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class PhoneLockingSettingsViewModel internal constructor(
    application: Application,
    private val dispatcher: CoroutineDispatcher,
    private val watchManager: WatchManager,
    private val dataStore: DataStore<Settings>
) : AndroidViewModel(application) {

    private val _phoneLockingMode = MutableLiveData(Settings.PhoneLockMode.DEVICE_ADMIN)

    val phoneLockingEnabled = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Boolean>(it, PHONE_LOCKING_ENABLED_KEY)
        } ?: liveData { }
    }
    val phoneLockingMode: LiveData<Settings.PhoneLockMode>
        get() = _phoneLockingMode

    val phoneLockingModes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        arrayOf(
            Pair(
                application.getString(R.string.phone_locking_mode_admin),
                Settings.PhoneLockMode.DEVICE_ADMIN
            ),
            Pair(
                application.getString(R.string.phone_locking_mode_accessibility),
                Settings.PhoneLockMode.ACCESSIBILITY_SERVICE
            )
        )
    } else {
        arrayOf(
            Pair(
                application.getString(R.string.phone_locking_mode_admin),
                Settings.PhoneLockMode.DEVICE_ADMIN
            )
        )
    }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Dispatchers.getIO(),
        WatchManager.getInstance(application),
        application.appSettingsStore
    )

    init {
        viewModelScope.launch {
            dataStore.data.map { it.phoneLockMode }.collect {
                _phoneLockingMode.postValue(it)
            }
        }
    }

    fun switchMode(mode: Settings.PhoneLockMode) {
        when (mode) {
            Settings.PhoneLockMode.ACCESSIBILITY_SERVICE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    switchToAccessibilityServiceMode()
                } else {
                    Timber.w("Unsupported SDK tried switching to accessibility service mode")
                }
            }
            Settings.PhoneLockMode.DEVICE_ADMIN -> switchToDeviceAdminMode()
        }
    }

    /**
     * Switch Phone Locking mode to Device Administrator. This disables Accessibility Service
     * components.
     */
    fun switchToDeviceAdminMode() {
        if (phoneLockingMode.value != Settings.PhoneLockMode.DEVICE_ADMIN) {
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
            _phoneLockingMode.postValue(Settings.PhoneLockMode.DEVICE_ADMIN)
            viewModelScope.launch(dispatcher) {
                dataStore.updateData {
                    it.copy(phoneLockMode = Settings.PhoneLockMode.DEVICE_ADMIN)
                }
                // Disable phone locking so the user is forced to re-enable and set up the new mode
                watchManager.settingsDatabase.boolPrefDao().updateAllForKey(
                    PHONE_LOCKING_ENABLED_KEY, false
                )
            }
        }
    }

    /**
     * Switch Phone Locking mode to Accessibility Service. This disables Device Administrator
     * components.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun switchToAccessibilityServiceMode() {
        if (phoneLockingMode.value != Settings.PhoneLockMode.ACCESSIBILITY_SERVICE) {
            val context = getApplication<Application>()
            Timber.i("Switching to Accessibility Service mode")
            if (context.isDeviceAdminEnabled()) {
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
            _phoneLockingMode.postValue(Settings.PhoneLockMode.ACCESSIBILITY_SERVICE)
            viewModelScope.launch(dispatcher) {
                dataStore.updateData {
                    it.copy(phoneLockMode = Settings.PhoneLockMode.DEVICE_ADMIN)
                }
                // Disable phone locking so the user is forced to re-enable and set up the new mode
                watchManager.settingsDatabase.boolPrefDao().updateAllForKey(
                    PHONE_LOCKING_ENABLED_KEY, false
                )
            }
        }
    }

    fun setPhoneLockingEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!,
                PHONE_LOCKING_ENABLED_KEY,
                isEnabled
            )
        }
    }

    fun canEnablePhoneLocking(): Boolean {
        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            phoneLockingMode.value == Settings.PhoneLockMode.ACCESSIBILITY_SERVICE
        ) {
            getApplication<Application>().isAccessibilityServiceEnabled()
        } else {
            getApplication<Application>().isDeviceAdminEnabled()
        }
    }
}
