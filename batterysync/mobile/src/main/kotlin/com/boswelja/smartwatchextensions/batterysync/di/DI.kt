package com.boswelja.smartwatchextensions.batterysync.di

import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.data.local.PhoneBatteryStatsDataSource
import com.boswelja.smartwatchextensions.batterysync.data.local.WatchBatteryStatsDbDataSource
import com.boswelja.smartwatchextensions.batterysync.data.repository.BatteryStatsRepositoryImpl
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
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
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
    single { PhoneBatteryStatsDataSource(androidContext()) }
    single { WatchBatteryStatsDbDataSource(get(), get(named("database"))) }

    // domain/repository
    single<BatteryStatsRepository> { BatteryStatsRepositoryImpl(get(), get()) }

    // domain/usecase
    single { GetBatteryChargeThreshold(get(), get()) }
    single { GetBatteryLowThreshold(get(), get()) }
    single { GetBatteryStats(get(), get(), get()) }
    single { GetBatterySyncEnabled(get(), get()) }
    single { GetPhoneBatteryStats(get()) }
    single { GetPhoneBatteryNotificationState(get(), get(), get()) }
    single { GetPhoneChargeNotificationEnabled(get(), get()) }
    single { GetPhoneLowNotificationEnabled(get(), get()) }
    single { GetWatchBatteryNotificationState(get(), get(), get()) }
    single { GetWatchChargeNotificationEnabled(get(), get()) }
    single { GetWatchLowNotificationEnabled(get(), get()) }

    single { SendUpdatedBatteryStatsToWatch(get()) }

    single { SetBatteryChargeThreshold(get(), get(), get()) }
    single { SetBatteryLowThreshold(get(), get(), get()) }
    single { SetBatterySyncEnabled(get(), get(), get(), get()) }
    single { SetPhoneChargeNotificationEnabled(get(), get(), get()) }
    single { SetPhoneLowNotificationEnabled(get(), get(), get()) }
    single { SetWatchChargeNotificationEnabled(get(), get()) }
    single { SetWatchLowNotificationEnabled(get(), get()) }

    single { StoreBatteryStatsForWatch(get(), get(), androidContext()) }

    // platform
    single<BatterySyncNotificationHandler> {
        MobileBatterySyncNotificationHandler(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            androidContext().getSystemService()!!
        )
    }

    // ui
    viewModel {
        BatterySyncViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { PhoneBatteryNotiSettingsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { WatchBatteryNotiSettingsViewModel(get(), get(), get(), get(), get(), get()) }
}
