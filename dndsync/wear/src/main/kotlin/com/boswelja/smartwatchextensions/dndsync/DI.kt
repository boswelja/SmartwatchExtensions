package com.boswelja.smartwatchextensions.dndsync

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * A Koin module for providing DnD Sync classes.
 */
val dndSyncModule = module {
    singleOf(::DnDSyncStateDsRepository) bind DnDSyncStateRepository::class
}
