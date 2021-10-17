package com.boswelja.smartwatchextensions.onboarding.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.settings.appSettingsStore
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class OnboardingViewModel(application: Application) : AndroidViewModel(application), DIAware {
    override val di: DI by closestDI()

    private val analytics: Analytics by instance()
    private val appSettingsStore = application.appSettingsStore

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            analytics.setAnalyticsEnabled(enabled)
            appSettingsStore.updateData { it.copy(analyticsEnabled = enabled) }
        }
    }
}
