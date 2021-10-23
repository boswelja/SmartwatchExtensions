package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.settings.Settings
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val analytics: Analytics,
    private val appSettingsStore: DataStore<Settings>
) : ViewModel() {

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            analytics.setAnalyticsEnabled(enabled)
            appSettingsStore.updateData { it.copy(analyticsEnabled = enabled) }
        }
    }
}
