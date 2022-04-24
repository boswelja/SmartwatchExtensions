package com.boswelja.smartwatchextensions.appmanager

/**
 * A repository for managing stored app icons.
 */
interface WatchAppIconRepository {

    /**
     * Store the given bytes corresponding to the given watch ID and package name. This will replace
     * any existing icons matching both watch UID and package name.
     * @param watchId The UID of the watch the icon belongs to.
     * @param packageName The app package name the icon belongs to.
     * @param iconBytes The raw icon file ByteArray.
     */
    suspend fun storeIconFor(watchId: String, packageName: String, iconBytes: ByteArray)

    /**
     * Get the icon bytes corresponding to the given watch ID and package name.
     * @param watchId The UID of the watch the icon belongs to.
     * @param packageName The app package name the icon belongs to.
     * @return The icon file bytes, or null if an icon wasn't found.
     */
    suspend fun retrieveIconFor(watchId: String, packageName: String): ByteArray?

    /**
     * Remove any stored icon for the given package on the given watch.
     * @param watchId The UID of the watch the package belongs to.
     * @param packageName The package name the icon belongs to.
     * @return true if the operation was successful, false otherwise.
     */
    suspend fun removeIconFor(watchId: String, packageName: String): Boolean
}
