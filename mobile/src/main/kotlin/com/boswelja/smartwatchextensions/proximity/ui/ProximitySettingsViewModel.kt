package com.boswelja.smartwatchextensions.proximity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.PHONE_PROXIMITY_NOTI_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.WATCH_PROXIMITY_NOTI_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.ExperimentalCoroutinesApi

class ProximitySettingsViewModel internal constructor(
    application: Application,
    watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )
    @ExperimentalCoroutinesApi
    val phoneProximityNotiSetting = watchManager.getBoolSetting(PHONE_PROXIMITY_NOTI_KEY)

    @ExperimentalCoroutinesApi
    val watchProximityNotiSetting = watchManager.getBoolSetting(WATCH_PROXIMITY_NOTI_KEY)
}
