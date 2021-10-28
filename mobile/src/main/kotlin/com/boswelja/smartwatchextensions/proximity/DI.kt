package com.boswelja.smartwatchextensions.proximity

import com.boswelja.smartwatchextensions.proximity.ui.ProximitySettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing proximity settings classes.
 */
val proximityModule = module {
    viewModel { ProximitySettingsViewModel(get(), get()) }
}
