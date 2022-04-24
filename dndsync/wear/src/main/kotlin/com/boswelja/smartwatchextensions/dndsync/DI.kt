package com.boswelja.smartwatchextensions.dndsync

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * A Koin module for providing DnD Sync classes.
 */
val dndSyncModule = module {
    single<DnDSyncStateRepository> { DnDSyncStateDsRepository(androidContext()) }
}
