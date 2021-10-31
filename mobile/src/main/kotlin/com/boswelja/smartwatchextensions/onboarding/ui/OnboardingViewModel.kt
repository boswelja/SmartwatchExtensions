package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.onboarding.CheckCompatibilityUseCase
import com.boswelja.smartwatchextensions.onboarding.ConfigureAnalyticsUseCase
import com.boswelja.smartwatchextensions.onboarding.RegisterWatchUseCase
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.launch

/**
 * A ViewModel for providing data to the onboarding flow.
 */
class OnboardingViewModel(
    private val configureAnalyticsUseCase: ConfigureAnalyticsUseCase,
    private val checkCompatibilityUseCase: CheckCompatibilityUseCase,
    private val registerWatchUseCase: RegisterWatchUseCase
) : ViewModel() {

    /**
     * Check whether the device supports any of the smartwatch platforms we support.
     * @param onFinished Called when the check has finished.
     */
    fun checkSmartwatchesSupported(onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isCompatible = checkCompatibilityUseCase.isCompatibleWithAnyPlatform()
            onFinished(isCompatible)
        }
    }

    /**
     * Set whether analytics are enabled.
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            configureAnalyticsUseCase.setAnalyticsEnabled(enabled)
        }
    }

    fun availableWatches() = registerWatchUseCase.availableWatches()

    suspend fun registerWatch(watch: Watch) = registerWatchUseCase.registerWatch(watch)
}
