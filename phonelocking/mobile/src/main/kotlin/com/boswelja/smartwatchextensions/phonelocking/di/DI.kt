package com.boswelja.smartwatchextensions.phonelocking.di

import com.boswelja.smartwatchextensions.phonelocking.domain.usecase.GetPhoneLockingEnabled
import com.boswelja.smartwatchextensions.phonelocking.domain.usecase.SetPhoneLockingEnabled
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingSettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * A Koin module for providing phone locking classes.
 */
val phoneLockingModule = module {
    singleOf(::GetPhoneLockingEnabled)
    singleOf(::SetPhoneLockingEnabled)
    viewModelOf(::PhoneLockingSettingsViewModel)
}
