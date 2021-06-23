package com.boswelja.smartwatchextensions.proximity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.PHONE_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.WATCH_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    @ExperimentalCoroutinesApi
    val phoneProximityNotiSetting = watchManager.getBoolSetting(PHONE_SEPARATION_NOTI_KEY)

    @ExperimentalCoroutinesApi
    val watchProximityNotiSetting = watchManager.getBoolSetting(WATCH_SEPARATION_NOTI_KEY)

    @ExperimentalCoroutinesApi
    suspend fun setPhoneProximityNotiEnabled(enabled: Boolean) {
        watchManager.selectedWatch.first()?.let {
            watchManager.updatePreference(it, PHONE_SEPARATION_NOTI_KEY, enabled)
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun setWatchProximityNotiEnabled(enabled: Boolean) {
        watchManager.selectedWatch.first()?.let {
            watchManager.updatePreference(it, WATCH_SEPARATION_NOTI_KEY, enabled)
        }
        if (enabled) {
            SeparationObserverService.start(getApplication())
        }
    }
}
