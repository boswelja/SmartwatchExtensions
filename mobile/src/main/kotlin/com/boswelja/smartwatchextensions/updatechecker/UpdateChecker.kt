package com.boswelja.smartwatchextensions.updatechecker

import android.content.Context

fun getUpdateChecker(context: Context): UpdateChecker {
    return GooglePlayUpdateChecker(context)
}

/**
 * An interface for a basic app update checker.
 */
interface UpdateChecker {

    /**
     * Checks whether a new version is available.
     * @return true if a new app version is available, false otherwise.
     */
    suspend fun isNewVersionAvailable(): Boolean

    /**
     * Starts the update flow.
     * @param context The [Context] that's requesting the launch.
     */
    fun launchDownloadScreen(context: Context)
}
