package com.boswelja.smartwatchextensions

import android.app.Application
import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.boswelja.smartwatchextensions.appmanager.appManagerModule
import com.boswelja.smartwatchextensions.batterysync.di.batterySyncModule
import com.boswelja.smartwatchextensions.core.coreModule
import com.boswelja.smartwatchextensions.dashboard.dashboardModule
import com.boswelja.smartwatchextensions.dndsync.dndSyncModule
import com.boswelja.smartwatchextensions.main.mainModule
import com.boswelja.smartwatchextensions.phonelocking.di.phoneLockingModule
import com.boswelja.smartwatchextensions.settings.appSettingsModule
import com.boswelja.smartwatchextensions.watchmanager.di.watchManagerModule
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

/**
 * A custom [Application] for starting Koin.
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            workManagerFactory()

            modules(
                coreModule,
                appManagerModule,
                batterySyncModule,
                dndSyncModule,
                clientsModule,
                databaseModule,
                watchManagerModule
            )

            modules(
                dashboardModule,
                mainModule,
                phoneLockingModule,
                appSettingsModule
            )
        }
    }
}

/**
 * A Koin module for providing watch connection clients.
 */
val clientsModule = module {
    single {
        Wearable.getMessageClient(get<Context>())
    }
    single {
        Wearable.getNodeClient(get<Context>())
    }
}

/**
 * A Koin module for providing database-related classes.
 */
val databaseModule = module {
    factory<SqlDriver> { params ->
        AndroidSqliteDriver(params.get(), get<Context>(), params.get())
    }
    single<CoroutineContext>(named("database")) { Dispatchers.IO }
}
