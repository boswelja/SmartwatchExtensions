package com.boswelja.smartwatchextensions.proximity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.PHONE_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.flow.first

class ProximitySettingsViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )
    val phoneProximityNotiSetting = watchManager.getBoolSetting(PHONE_SEPARATION_NOTI_KEY)

    val watchProximityNotiSetting = watchManager.getBoolSetting(WATCH_SEPARATION_NOTI_KEY)

    suspend fun setPhoneProximityNotiEnabled(enabled: Boolean) {
        watchManager.selectedWatch.first()?.let {
            watchManager.updatePreference(it, PHONE_SEPARATION_NOTI_KEY, enabled)
        }
    }

    suspend fun setWatchProximityNotiEnabled(enabled: Boolean) {
        watchManager.selectedWatch.first()?.let {
            watchManager.updatePreference(it, WATCH_SEPARATION_NOTI_KEY, enabled)
        }
        if (enabled) {
            SeparationObserverService.start(getApplication())
        }
    }
}
