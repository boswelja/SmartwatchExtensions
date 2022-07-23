package com.boswelja.smartwatchextensions.phonelocking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.phonelocking.domain.usecase.GetPhoneLockingEnabled
import com.boswelja.smartwatchextensions.phonelocking.domain.usecase.SetPhoneLockingEnabled
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to Phone Locking settings.
 */
class PhoneLockingSettingsViewModel(
    getPhoneLockingEnabled: GetPhoneLockingEnabled,
    private val setPhoneLockingEnabled: SetPhoneLockingEnabled
) : ViewModel() {

    /**
     * Flow whether phone locking is enabled for the selected watch.
     */
    val phoneLockingEnabled = getPhoneLockingEnabled()
        .map { it.getOrDefault(false) }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

    /**
     * Set whether phone locking is enabled.
     */
    fun setPhoneLockingEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setPhoneLockingEnabled.invoke(isEnabled)
        }
    }
}
