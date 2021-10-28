package com.boswelja.smartwatchextensions.onboarding

import android.content.Context
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingViewModel
import com.boswelja.smartwatchextensions.settings.appSettingsStore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module to provide onboarding classes.
 */
val onboardingModule = module {
    viewModel {
        OnboardingViewModel(
            get(),
            get<Context>().appSettingsStore,
            CheckCompatibilityUseCase(get())
        )
    }
}
