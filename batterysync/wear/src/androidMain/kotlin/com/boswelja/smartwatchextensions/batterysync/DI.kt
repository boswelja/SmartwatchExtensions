package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.ui.BatteryStatsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val batterySyncModule = module {
    single<BatteryStatsRepository> { BatteryStatsDsRepository(get()) }
    single<BatterySyncStateRepository> { BatterySyncStateDsRepository(get()) }
    viewModel { BatteryStatsViewModel(get(), get(), get(), get()) }
}
