package com.boswelja.smartwatchextensions.phonelocking.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.phonelocking.Utils.isAccessibilityServiceEnabled
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class PhoneLockingSettingsViewModel internal constructor(
    application: Application,
    private val dispatcher: CoroutineDispatcher,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    val phoneLockingEnabled = watchManager.selectedWatch.flatMapLatest {
        it?.let { watch ->
            watchManager.getBoolSetting(PHONE_LOCKING_ENABLED_KEY, watch)
        } ?: flow { }
    }

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Dispatchers.IO,
        WatchManager.getInstance(application)
    )

    fun setPhoneLockingEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!,
                PHONE_LOCKING_ENABLED_KEY,
                isEnabled
            )
        }
    }

    fun canEnablePhoneLocking(): Boolean {
        return getApplication<Application>().isAccessibilityServiceEnabled()
    }
}
