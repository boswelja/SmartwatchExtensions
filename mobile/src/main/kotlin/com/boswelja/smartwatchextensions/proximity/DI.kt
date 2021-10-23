package com.boswelja.smartwatchextensions.proximity

import com.boswelja.smartwatchextensions.proximity.ui.ProximitySettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val proximityModule = module {
    viewModel { ProximitySettingsViewModel(get(), get()) }
}
