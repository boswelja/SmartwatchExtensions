package com.boswelja.smartwatchextensions.batterysync.di

import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.data.BatteryStatsDsRepository
import com.boswelja.smartwatchextensions.batterysync.data.BatterySyncConfigDsRepository
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatterySyncConfig
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatterySyncEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.RequestBatteryStatsUpdate
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SendBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetPhoneBatteryStats
import com.boswelja.smartwatchextensions.batterysync.platform.WearBatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.ui.BatteryStatsViewModel
import com.boswelja.smartwatchextensions.core.devicemanagement.phoneStateStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * A Koin module for providing Battery Sync classes.
 */
val batterySyncModule = module {
    // Repositories
    singleOf(::BatteryStatsDsRepository) bind BatteryStatsRepository::class
    singleOf(::BatterySyncConfigDsRepository) bind BatterySyncConfigRepository::class

    // Use cases
    singleOf(::GetBatterySyncConfig)
    singleOf(::GetBatterySyncEnabled)
    singleOf(::GetPhoneBatteryStats)
    singleOf(::RequestBatteryStatsUpdate)
    singleOf(::SendBatteryStats)
    singleOf(::SetPhoneBatteryStats)

    single<BatterySyncNotificationHandler> {
        WearBatterySyncNotificationHandler(
            get(),
            androidContext().phoneStateStore,
            androidContext(),
            androidContext().getSystemService()!!
        )
    }
    viewModelOf(::BatteryStatsViewModel)
}
