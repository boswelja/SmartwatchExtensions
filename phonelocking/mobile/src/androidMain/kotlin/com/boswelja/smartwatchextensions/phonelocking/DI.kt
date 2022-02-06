package com.boswelja.smartwatchextensions.phonelocking

import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing phone locking classes.
 */
val phoneLockingModule = module {
    viewModel { PhoneLockingSettingsViewModel(get(), get(), get()) }
}
