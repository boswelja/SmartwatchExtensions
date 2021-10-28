package com.boswelja.smartwatchextensions.dndsync.ui

import android.app.NotificationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.DND_SYNC_TO_WATCH_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.DND_SYNC_WITH_THEATER_KEY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data for DnD Sync.
 * @param watchManager [WatchManager].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DnDSyncSettingsViewModel(
    private val notificationManager: NotificationManager,
    val watchManager: WatchManager
) : ViewModel() {

    /**
     * Flow whether the selected watch can send DnD status.
     */
    val canSendDnD = watchManager.selectedWatchHasCapability(Capability.SEND_DND)

    /**
     * Flow whether the selected watch can receive DnD status.
     */
    val canReceiveDnD = watchManager.selectedWatchHasCapability(Capability.RECEIVE_DND)

    /**
     * Flow whether DnD Sync to Phone is enabled.
     */
    val syncToPhone = watchManager.getBoolSetting(DND_SYNC_TO_PHONE_KEY)

    /**
     * Flow whether DnD Sync to Watch is enabled.
     */
    val syncToWatch = watchManager.getBoolSetting(DND_SYNC_TO_WATCH_KEY)

    /**
     * Flow whether DnD Sync with Theater is enabled.
     */
    val syncWithTheater = watchManager.getBoolSetting(DND_SYNC_WITH_THEATER_KEY)

    /**
     * Get whether the device has notification policy access.
     */
    val hasNotificationPolicyAccess: Boolean
        get() = notificationManager.isNotificationPolicyAccessGranted

    /**
     * Set whether DnD Sync to Phone is enabled.
     */
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

    /**
     * Set whether DnD Sync to Watch is enabled.
     */
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

    /**
     * Set whether DnD Sync with Theater is enabled.
     */
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
