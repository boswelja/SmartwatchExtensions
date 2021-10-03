package com.boswelja.smartwatchextensions.dndsync.ui

import android.app.Application
import android.app.NotificationManager
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DnDSyncSettingsViewModel internal constructor(
    application: Application,
    val watchManager: WatchManager,
    private val notificationManager: NotificationManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        application.getSystemService<NotificationManager>()!!
    )

    val canSendDnD = watchManager.selectedWatchHasCapability(Capability.SEND_DND)
    val canReceiveDnD = watchManager.selectedWatchHasCapability(Capability.RECEIVE_DND)

    val syncToPhone = watchManager.getBoolSetting(DND_SYNC_TO_PHONE_KEY)
    val syncToWatch = watchManager.getBoolSetting(DND_SYNC_TO_WATCH_KEY)
    val syncWithTheater = watchManager.getBoolSetting(DND_SYNC_WITH_THEATER_KEY)

    val hasNotificationPolicyAccess: Boolean
        get() = notificationManager.isNotificationPolicyAccessGranted

    fun setSyncToPhone(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!,
                DND_SYNC_TO_PHONE_KEY,
                isEnabled
            )
        }
    }

    fun setSyncToWatch(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!,
                DND_SYNC_TO_WATCH_KEY,
                isEnabled
            )
        }
    }

    fun setSyncWithTheater(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!,
                DND_SYNC_WITH_THEATER_KEY,
                isEnabled
            )
        }
    }
}
