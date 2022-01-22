package com.boswelja.smartwatchextensions.phonelocking

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * A Koin module for providing Phone Locking classes
 */
val phoneLockingModule = module {
    single<PhoneLockingStateRepository> { PhoneLockingStateDsRepository(androidContext()) }
}
