package com.boswelja.smartwatchextensions.phonelocking

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val phoneLockingModule = module {
    single<PhoneLockingStateRepository> { PhoneLockingStateDsRepository(androidContext()) }
}
