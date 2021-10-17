package com.boswelja.smartwatchextensions.proximity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.PHONE_SEPARATION_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.WATCH_SEPARATION_NOTI_KEY
import kotlinx.coroutines.flow.first
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class ProximitySettingsViewModel(
    application: Application
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val watchManager: WatchManager by instance()

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
