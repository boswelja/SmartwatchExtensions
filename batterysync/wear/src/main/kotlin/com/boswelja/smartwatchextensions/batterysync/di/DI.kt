package com.boswelja.smartwatchextensions.batterysync

import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.batterysync.data.BatteryStatsDsRepository
import com.boswelja.smartwatchextensions.batterysync.data.BatterySyncStateDsRepository
import com.boswelja.smartwatchextensions.batterysync.domain.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.domain.BatterySyncStateRepository
import com.boswelja.smartwatchextensions.batterysync.platform.WearBatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.ui.BatteryStatsViewModel
import com.boswelja.smartwatchextensions.core.devicemanagement.phoneStateStore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing Battery Sync classes.
 */
val batterySyncModule = module {
    single<BatteryStatsRepository> { BatteryStatsDsRepository(get()) }
    single<BatterySyncStateRepository> { BatterySyncStateDsRepository(get()) }
    single<BatterySyncNotificationHandler> {
        WearBatterySyncNotificationHandler(
            get(),
            androidContext().phoneStateStore,
            androidContext(),
            androidContext().getSystemService()!!
        )
    }
    viewModel { BatteryStatsViewModel(get(), get(), get(), get()) }
}
