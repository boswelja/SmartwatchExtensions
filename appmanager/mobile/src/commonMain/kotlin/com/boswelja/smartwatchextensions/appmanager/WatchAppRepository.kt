package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing apps installed on watches.
 */
interface WatchAppRepository {

    /**
     * Update the icon Uri for a given package on a given watch.
     * @param watchId The UID of the watch that owns the package.
     * @param packageName The package name of the app.
     * @param iconUri The new icon Uri.
     */
    suspend fun setIconFor(watchId: String, packageName: String, iconUri: String?)

    /**
     * Store a list of [WatchAppDetails] in the repository. Existing packages will be replaced.
     * @param apps The [List] of [WatchAppDetails].
     */
    suspend fun updateAll(apps: List<WatchAppDetails>)

    /**
     * Delete all stored apps linked to the device with the given UID.
     * @param watchId The target UID whose apps should be deleted.
     */
    suspend fun deleteFor(watchId: String)

    /**
     * Delete a single package for a device with the given UID.
     * @param watchId The target UID the package belongs to.
     * @param packageName The app package name to delete.
     */
    suspend fun delete(watchId: String, packageName: String)

    /**
     * Flow a [WatchAppDetails] for a package on a given device.
     * @param watchId The target UID the package belongs to.
     * @param packageName The app package name to get details for.
     */
    fun getDetailsFor(watchId: String, packageName: String): Flow<WatchAppDetails>

    /**
     * Flow a list of apps found on the given device.
     * @param watchId The target device UID to get apps for.
     */
    fun getAppsFor(watchId: String): Flow<List<WatchApp>>

    /**
     * Flow a list of app versions for the given device.
     * @param watchId The target device UID to get app versions for.
     */
    fun getAppVersionsFor(watchId: String): Flow<List<WatchAppVersion>>

    /**
     * Flow the app count for the given device.
     * @param watchId The target device UID to count apps for.
     */
    fun countFor(watchId: String): Flow<Long>
}
