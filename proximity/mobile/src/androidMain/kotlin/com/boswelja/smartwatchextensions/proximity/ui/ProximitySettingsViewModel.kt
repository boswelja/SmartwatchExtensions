package com.boswelja.smartwatchextensions.proximity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.proximity.common.ProximitySettingKeys.PHONE_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.proximity.common.ProximitySettingKeys.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to Proximity Settings.
 */
class ProximitySettingsViewModel(
    application: Application,
    private val selectedWatchManager: SelectedWatchManager,
    private val settingsRepository: WatchSettingsRepository
) : AndroidViewModel(application) {

    /**
     * Flow whether phone separation alerts are enabled.
     */
    val phoneProximityNotiSetting = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, PHONE_SEPARATION_NOTI_KEY)
    }

    /**
     * Flow whether watch separation alerts are enabled.
     */
    val watchProximityNotiSetting = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, WATCH_SEPARATION_NOTI_KEY)
    }

    /**
     * Set whether phone separation alerts are enabled.
     */
    fun setPhoneProximityNotiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            selectedWatchManager.selectedWatch.first()?.let {
                settingsRepository.putBoolean(it.uid, PHONE_SEPARATION_NOTI_KEY, enabled)
            }
        }
    }

    /**
     * Set whether watch separation alerts are enabled.
     */
    fun setWatchProximityNotiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            selectedWatchManager.selectedWatch.first()?.let {
                settingsRepository.putBoolean(it.uid, WATCH_SEPARATION_NOTI_KEY, enabled)
            }
            if (enabled) {
                SeparationObserverService.start(getApplication())
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> mapStateForSelectedWatch(
        defaultValue: T,
        block: (Watch) -> Flow<T>
    ): StateFlow<T> =
        selectedWatchManager.selectedWatch
            .filterNotNull()
            .flatMapLatest(block)
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                defaultValue
            )
}
