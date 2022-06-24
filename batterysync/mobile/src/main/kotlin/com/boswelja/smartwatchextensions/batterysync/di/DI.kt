package com.boswelja.smartwatchextensions.batterysync.di

import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.data.local.BatteryStatsDbDataSource
import com.boswelja.smartwatchextensions.batterysync.data.repository.BatteryStatsRepositoryImpl
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatterySyncEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneLowNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchLowNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.platform.MobileBatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncViewModel
import com.boswelja.smartwatchextensions.batterysync.ui.phonebatterynoti.PhoneBatteryNotiSettingsViewModel
import com.boswelja.smartwatchextensions.batterysync.ui.watchbatterynoti.WatchBatteryNotiSettingsViewModel
import org.koin.android.ext.koin.androidApplication
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
    single { BatteryStatsDbDataSource(get(), get(named("database"))) }

    // domain
    single<BatteryStatsRepository> { BatteryStatsRepositoryImpl(get()) }
    single { GetBatteryStats(get(), get(), get()) }
    single { GetBatterySyncEnabled(get(), get()) }
    single { GetBatteryChargeThreshold(get(), get()) }
    single { GetBatteryLowThreshold(get(), get()) }
    single { GetWatchChargeNotificationEnabled(get(), get()) }
    single { GetWatchLowNotificationEnabled(get(), get()) }
    single { GetPhoneChargeNotificationEnabled(get(), get()) }
    single { GetPhoneLowNotificationEnabled(get(), get()) }
    single { GetPhoneBatteryNotificationState(get(), get(), get()) }
    single { GetWatchBatteryNotificationState(get(), get(), get()) }

    // platform
    single<BatterySyncNotificationHandler> {
        MobileBatterySyncNotificationHandler(get(), get(), androidContext(), androidContext().getSystemService()!!)
    }

    // ui
    viewModel { BatterySyncViewModel(androidApplication(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PhoneBatteryNotiSettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { WatchBatteryNotiSettingsViewModel(get(), get(), get(), get(), get(), get()) }
}
