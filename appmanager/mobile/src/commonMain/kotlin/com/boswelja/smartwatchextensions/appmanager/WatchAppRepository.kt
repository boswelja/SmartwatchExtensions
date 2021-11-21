package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing apps installed on watches.
 */
interface WatchAppRepository {

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
     * Delete a list of package names for the device with the given UID.
     * @param watchId The target UID to delete packages for.
     * @param packages The list of package names to remove.
     */
    suspend fun delete(watchId: String, packages: List<String>)

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
