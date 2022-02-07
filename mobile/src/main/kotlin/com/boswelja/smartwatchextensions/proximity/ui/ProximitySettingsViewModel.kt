package com.boswelja.smartwatchextensions.proximity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.proximity.common.ProximitySettingKeys.PHONE_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.proximity.common.ProximitySettingKeys.WATCH_SEPARATION_NOTI_KEY
import kotlinx.coroutines.flow.first

/**
 * A ViewModel for providing data to Proximity Settings.
 */
class ProximitySettingsViewModel(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    /**
     * Flow whether phone separation alerts are enabled.
     */
    val phoneProximityNotiSetting = watchManager.getBoolSetting(PHONE_SEPARATION_NOTI_KEY)

    /**
     * Flow whether watch separation alerts are enabled.
     */
    val watchProximityNotiSetting = watchManager.getBoolSetting(WATCH_SEPARATION_NOTI_KEY)

    /**
     * Set whether phone separation alerts are enabled.
     */
    suspend fun setPhoneProximityNotiEnabled(enabled: Boolean) {
        watchManager.selectedWatch.first()?.let {
            watchManager.updatePreference(it, PHONE_SEPARATION_NOTI_KEY, enabled)
        }
    }

    /**
     * Set whether watch separation alerts are enabled.
     */
    suspend fun setWatchProximityNotiEnabled(enabled: Boolean) {
        watchManager.selectedWatch.first()?.let {
            watchManager.updatePreference(it, WATCH_SEPARATION_NOTI_KEY, enabled)
        }
        if (enabled) {
            SeparationObserverService.start(getApplication())
        }
    }
}
