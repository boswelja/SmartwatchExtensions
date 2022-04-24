package com.boswelja.smartwatchextensions.appmanager

/**
 * An implementation of [WatchAppIconRepository] that works with a single map in memory.
 */
class InMemoryWatchAppIconRepository : WatchAppIconRepository {
    val iconMap = mutableMapOf<Pair<String, String>, ByteArray>()

    override suspend fun storeIconFor(
        watchId: String,
        packageName: String,
        iconBytes: ByteArray
    ) {
        iconMap[Pair(watchId, packageName)] = iconBytes
    }

    override suspend fun retrieveIconFor(
        watchId: String,
        packageName: String
    ): ByteArray? = iconMap[Pair(watchId, packageName)]

    override suspend fun removeIconFor(
        watchId: String,
        packageName: String
    ): Boolean = iconMap.remove(Pair(watchId, packageName)) != null
}
