package com.boswelja.smartwatchextensions.dndsync.ui

import android.app.Application
import android.app.NotificationManager
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.DND_SYNC_TO_WATCH_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.DND_SYNC_WITH_THEATER_KEY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

@OptIn(ExperimentalCoroutinesApi::class)
class DnDSyncSettingsViewModel internal constructor(
    application: Application,
    private val notificationManager: NotificationManager
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    val watchManager: WatchManager by instance()

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
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
