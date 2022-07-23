package com.boswelja.smartwatchextensions.phonelocking.di

import com.boswelja.smartwatchextensions.phonelocking.domain.usecase.GetPhoneLockingEnabled
import com.boswelja.smartwatchextensions.phonelocking.domain.usecase.SetPhoneLockingEnabled
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing phone locking classes.
 */
val phoneLockingModule = module {
    single { GetPhoneLockingEnabled(get(), get()) }
    single { SetPhoneLockingEnabled(get(), get(), get()) }
    viewModel { PhoneLockingSettingsViewModel(get(), get()) }
}
