package com.boswelja.smartwatchextensions.phonelocking.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.phonelocking.isAccessibilityServiceEnabled
import com.boswelja.smartwatchextensions.settings.BoolSetting
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.settings.UPDATE_BOOL_PREFERENCE
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to Phone Locking settings.
 */
class PhoneLockingSettingsViewModel(
    application: Application,
    messageClient: MessageClient,
    private val selectedWatchManager: SelectedWatchManager,
    private val settingsRepository: WatchSettingsRepository
) : AndroidViewModel(application) {

    private val boolMessageHandler = MessageHandler(BoolSettingSerializer, messageClient)

    /**
     * Flow whether phone locking is enabled for the selected watch.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val phoneLockingEnabled = selectedWatchManager.selectedWatch
        .filterNotNull()
        .flatMapLatest {
            settingsRepository.getBoolean(it.uid, PHONE_LOCKING_ENABLED_KEY, false)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

    /**
     * Flows the currently selected watch name. Defaults to "Watch".
     */
    val watchName = selectedWatchManager.selectedWatch
        .filterNotNull()
        .map { it.name }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            "Watch"
        )

    /**
     * Set whether phone locking is enabled.
     */
    fun setPhoneLockingEnabled(isEnabled: Boolean): Boolean {
        // Return false if we're trying to enable phone locking but we can't
        if (isEnabled && !canEnablePhoneLocking()) return false

        viewModelScope.launch(Dispatchers.IO) {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            settingsRepository.putBoolean(
                selectedWatch!!.uid,
                PHONE_LOCKING_ENABLED_KEY,
                isEnabled
            )
            boolMessageHandler.sendMessage(
                selectedWatch.uid,
                Message(
                    UPDATE_BOOL_PREFERENCE,
                    BoolSetting(PHONE_LOCKING_ENABLED_KEY, isEnabled)
                )
            )
        }

        return true
    }

    fun canEnablePhoneLocking(): Boolean {
        return getApplication<Application>().isAccessibilityServiceEnabled()
    }
}
