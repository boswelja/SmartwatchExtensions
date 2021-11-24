package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * An implementation of [WatchAppRepository] that works with a single MutableStateFlow in memory.
 */
class InMemoryWatchAppRepository : WatchAppRepository {

    val appList = MutableStateFlow<List<WatchAppDetails>>(emptyList())

    override suspend fun updateAll(apps: List<WatchAppDetails>) {
        appList.update { it + apps }
    }

    override suspend fun deleteFor(watchId: String) {
        appList.update { it.filterNot { app -> app.watchId == watchId } }
    }

    override suspend fun delete(watchId: String, packageName: String) {
        appList.update { list ->
            list.filterNot { it.watchId == watchId && it.packageName == packageName }
        }
    }

    override suspend fun delete(watchId: String, packages: List<String>) {
        appList.update { list ->
            list.filterNot { it.watchId == watchId && packages.contains(it.packageName) }
        }
    }

    override fun getDetailsFor(watchId: String, packageName: String): Flow<WatchAppDetails> =
        appList.map { list ->
            list.first { it.watchId == watchId && it.packageName == packageName }
        }

    override fun getAppsFor(watchId: String): Flow<List<WatchApp>> = appList.map { list ->
        list
            .filter { it.watchId == watchId }
            .map { WatchApp(it.packageName, it.label, it.versionName, it.isSystemApp, it.isEnabled) }
    }

    override fun getAppVersionsFor(watchId: String): Flow<List<WatchAppVersion>> = appList
        .map { list ->
            list
                .filter { it.watchId == watchId }
                .map { WatchAppVersion(it.packageName, it.versionCode) }
        }

    override fun countFor(watchId: String): Flow<Long> = appList
        .map { list -> list.count { it.watchId == watchId }.toLong() }
}
