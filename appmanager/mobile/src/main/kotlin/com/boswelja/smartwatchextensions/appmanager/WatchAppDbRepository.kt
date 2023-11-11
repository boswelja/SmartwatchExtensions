package com.boswelja.smartwatchextensions.appmanager

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A [WatchAppRepository] backed by a SQLDelight database implementation.
 * @param database The [WatchAppDatabase] implementation to call.
 * @param dispatcher The [CoroutineContext] to move blocking calls to.
 */
class WatchAppDbRepository(
    private val database: WatchAppDatabase,
    private val dispatcher: CoroutineContext
) : WatchAppRepository {
    override suspend fun updateAll(apps: List<WatchAppDetails>) {
        withContext(dispatcher) {
            database.watchAppQueries.transaction {
                apps.forEach { app ->
                    database.watchAppQueries.insert(
                        WatchAppDb(
                            app.watchId,
                            app.packageName,
                            app.label,
                            app.versionName,
                            app.versionCode,
                            app.isSystemApp,
                            app.isLaunchable,
                            app.isEnabled,
                            app.installTime,
                            app.updateTime,
                            app.permissions
                        )
                    )
                }
            }
        }
    }

    override suspend fun deleteFor(watchId: String) {
        withContext(dispatcher) {
            database.watchAppQueries.removeFor(watchId)
        }
    }

    override suspend fun delete(watchId: String, packageName: String) {
        withContext(dispatcher) {
            database.watchAppQueries.remove(watchId, packageName)
        }
    }

    override suspend fun delete(watchId: String, packages: List<String>) {
        withContext(dispatcher) {
            database.watchAppQueries.transaction {
                packages.forEach { packageName ->
                    database.watchAppQueries.remove(watchId, packageName)
                }
            }
        }
    }

    override fun getDetailsFor(watchId: String, packageName: String): Flow<WatchAppDetails> =
        database.watchAppQueries
            .getDetailsFor(watchId, packageName) {
                watch_id,
                package_name,
                label,
                version_name,
                version_code,
                system_app,
                launchable,
                enabled,
                install_time,
                update_time,
                permissions ->
                WatchAppDetails(
                    watchId = watch_id,
                    packageName = package_name,
                    label = label,
                    versionName = version_name,
                    versionCode = version_code,
                    isSystemApp = system_app,
                    isLaunchable = launchable,
                    isEnabled = enabled,
                    installTime = install_time,
                    updateTime = update_time,
                    permissions = permissions
                )
            }
            .asFlow()
            .mapToOne(dispatcher)

    override fun getAppsFor(watchId: String): Flow<List<WatchApp>> =
        database.watchAppQueries
            .getDisplayItemsFor(watchId) { package_name, label, version_name, system_app, enabled ->
                WatchApp(package_name, label, version_name, system_app, enabled)
            }
            .asFlow()
            .mapToList(dispatcher)

    override fun getAppVersionsFor(watchId: String): Flow<List<WatchAppVersion>> =
        database.watchAppQueries
            .getVersionsFor(watchId) { package_name, version_code ->
                WatchAppVersion(package_name, version_code)
            }
            .asFlow()
            .mapToList(dispatcher)

    override fun countFor(watchId: String): Flow<Long> =
        database.watchAppQueries
            .countFor(watchId)
            .asFlow()
            .mapToOne(dispatcher)
}
