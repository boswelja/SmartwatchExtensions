package com.boswelja.smartwatchextensions.phonelocking.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.phonelocking.Utils.isAccessibilityServiceEnabled
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneLockingSettingsViewModel internal constructor(
    application: Application,
    private val dispatcher: CoroutineDispatcher,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    val phoneLockingEnabled = watchManager.selectedWatch.switchMap {
        it?.let { watch ->
            watchManager.getPreferenceObservable<Boolean>(watch.id, PHONE_LOCKING_ENABLED_KEY)
        } ?: liveData { }
    }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Dispatchers.IO,
        WatchManager.getInstance(application)
    )

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
        return getApplication<Application>().isAccessibilityServiceEnabled()
    }
}
