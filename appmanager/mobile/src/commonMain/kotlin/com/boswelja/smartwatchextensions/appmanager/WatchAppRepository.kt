package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.flow.Flow

interface WatchAppRepository {
    suspend fun updateAll(apps: List<WatchAppDetails>)

    suspend fun deleteFor(watchId: String)

    suspend fun delete(watchId: String, packageName: String)

    fun getDetailsFor(watchId: String, packageName: String): Flow<WatchAppDetails>

    fun getAppsFor(watchId: String): Flow<List<WatchApp>>

    fun getAppVersionsFor(watchId: String): Flow<List<WatchAppVersion>>

    fun countFor(watchId: String): Flow<Long>
}
