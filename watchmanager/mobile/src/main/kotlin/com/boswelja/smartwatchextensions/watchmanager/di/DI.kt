package com.boswelja.smartwatchextensions.watchmanager.di

import com.boswelja.smartwatchextensions.core.watches.available.AvailableWatchRepositoryImpl
import com.boswelja.smartwatchextensions.watchmanager.data.WatchVersionRepositoryImpl
import com.boswelja.smartwatchextensions.core.watches.available.AvailableWatchRepository
import com.boswelja.smartwatchextensions.watchmanager.domain.WatchVersionRepository
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchViewModel
import com.boswelja.smartwatchextensions.watchmanager.ui.WatchManagerViewModel
import com.boswelja.smartwatchextensions.watchmanager.ui.manageregistered.ManageRegisteredWatchViewModel
import com.boswelja.smartwatchextensions.watchmanager.ui.pick.WatchPickerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val watchManagerModule = module {
    singleOf(::WatchVersionRepositoryImpl) bind WatchVersionRepository::class
    singleOf(::AvailableWatchRepositoryImpl) bind AvailableWatchRepository::class

    viewModelOf(::WatchManagerViewModel)
    viewModelOf(::RegisterWatchViewModel)
    viewModelOf(::ManageRegisteredWatchViewModel)
    viewModelOf(::WatchPickerViewModel)
}
