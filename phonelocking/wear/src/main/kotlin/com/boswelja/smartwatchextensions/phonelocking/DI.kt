package com.boswelja.smartwatchextensions.phonelocking

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * A Koin module for providing Phone Locking classes
 */
val phoneLockingModule = module {
    singleOf(::PhoneLockingStateDsRepository) bind PhoneLockingStateRepository::class
}
