package com.boswelja.smartwatchextensions.batterysync.di

import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.data.batterystats.BatteryStatsRepositoryImpl
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatterySyncEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneLowNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchLowNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SendUpdatedBatteryStatsToWatch
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetBatterySyncEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetPhoneChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetPhoneLowNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetWatchChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetWatchLowNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.StoreBatteryStatsForWatch
import com.boswelja.smartwatchextensions.batterysync.platform.MobileBatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncViewModel
import com.boswelja.smartwatchextensions.batterysync.ui.phonebatterynoti.PhoneBatteryNotiSettingsViewModel
import com.boswelja.smartwatchextensions.batterysync.ui.watchbatterynoti.WatchBatteryNotiSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * A Koin module for providing Battery Sync classes.
 */
val batterySyncModule = module {
    // data
    single {
        BatteryStatsDatabase(
            get { parametersOf(BatteryStatsDatabase.Schema, "batterystats.db") }
        )
    }

    // domain/repository
    single<BatteryStatsRepository> { BatteryStatsRepositoryImpl(get(), get(), get(named("database"))) }

    // domain/usecase
    singleOf(::GetBatteryChargeThreshold)
    singleOf(::GetBatteryLowThreshold)
    singleOf(::GetBatteryStats)
    singleOf(::GetBatterySyncEnabled)
    singleOf(::GetPhoneBatteryStats)
    singleOf(::GetPhoneBatteryNotificationState)
    singleOf(::GetPhoneChargeNotificationEnabled)
    singleOf(::GetPhoneLowNotificationEnabled)
    singleOf(::GetWatchBatteryNotificationState)
    singleOf(::GetWatchChargeNotificationEnabled)
    singleOf(::GetWatchLowNotificationEnabled)

    singleOf(::SendUpdatedBatteryStatsToWatch)

    singleOf(::SetBatteryChargeThreshold)
    singleOf(::SetBatteryLowThreshold)
    singleOf(::SetBatterySyncEnabled)
    singleOf(::SetPhoneChargeNotificationEnabled)
    singleOf(::SetPhoneLowNotificationEnabled)
    singleOf(::SetWatchChargeNotificationEnabled)
    singleOf(::SetWatchLowNotificationEnabled)

    singleOf(::StoreBatteryStatsForWatch)

    // platform
    singleOf(::MobileBatterySyncNotificationHandler) bind BatterySyncNotificationHandler::class

    // ui
    viewModelOf(::BatterySyncViewModel)
    viewModelOf(::PhoneBatteryNotiSettingsViewModel)
    viewModelOf(::WatchBatteryNotiSettingsViewModel)
}
