package com.boswelja.smartwatchextensions.batterysync.di

import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.data.local.BatteryStatsDbDataSource
import com.boswelja.smartwatchextensions.batterysync.data.repository.BatteryStatsRepositoryImpl
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
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
    single {
        BatteryStatsDatabase(
            get { parametersOf(BatteryStatsDatabase.Schema, "batterystats.db") }
        )
    }
    single<BatteryStatsRepository> { BatteryStatsRepositoryImpl(get()) }
    single<BatterySyncNotificationHandler> {
        MobileBatterySyncNotificationHandler(get(), get(), androidContext(), androidContext().getSystemService()!!)
    }
    single { BatteryStatsDbDataSource(get(), get(named("database"))) }
    viewModel { BatterySyncViewModel(androidApplication(), get(), get(), get(), get(), get()) }
    viewModel { PhoneBatteryNotiSettingsViewModel(get(), get(), get()) }
    viewModel { WatchBatteryNotiSettingsViewModel(get(), get()) }
}
