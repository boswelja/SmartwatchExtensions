package com.boswelja.smartwatchextensions.dndsync.ui

import android.app.NotificationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.core.watches.Watch
import com.boswelja.smartwatchextensions.core.watches.capability.WatchCapabilityRepository
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.dndsync.SendDnDCapability
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
 * A ViewModel for providing data for DnD Sync.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DnDSyncSettingsViewModel(
    private val notificationManager: NotificationManager,
    private val selectedWatchController: SelectedWatchController,
    private val settingsRepository: WatchSettingsRepository,
    private val watchCapabilityRepository: WatchCapabilityRepository
) : ViewModel() {

    /**
     * Flow whether the selected watch can send DnD status.
     */
    val canSendDnD = mapStateForSelectedWatch(false) {
        watchCapabilityRepository.hasCapability(it.uid, SendDnDCapability)
    }

    /**
     * Flow whether DnD Sync to Phone is enabled.
     */
    val syncToPhone = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, DND_SYNC_TO_PHONE_KEY)
    }

    /**
     * Flow whether DnD Sync with Theater is enabled.
     */
    val syncWithTheater = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, DND_SYNC_WITH_THEATER_KEY)
    }

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
            val selectedWatch = selectedWatchController.selectedWatch.first()
            settingsRepository.putBoolean(
                selectedWatch!!.uid,
                DND_SYNC_TO_PHONE_KEY,
                isEnabled
            )
        }
    }

    /**
     * Set whether DnD Sync with Theater is enabled.
     */
    fun setSyncWithTheater(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchController.selectedWatch.first()
            settingsRepository.putBoolean(
                selectedWatch!!.uid,
                DND_SYNC_WITH_THEATER_KEY,
                isEnabled
            )
        }
    }

    private fun <T> mapStateForSelectedWatch(
        defaultValue: T,
        block: (Watch) -> Flow<T>
    ): StateFlow<T> =
        selectedWatchController.selectedWatch
            .filterNotNull()
            .flatMapLatest(block)
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                defaultValue
            )
}
