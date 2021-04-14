@file:Suppress("DEPRECATION")

package com.boswelja.smartwatchextensions.bootorupdate.updater

import android.content.Context
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.appStateStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Updater(private val context: Context) {

    private var lastAppVersion: Int = BuildConfig.VERSION_CODE

    suspend fun checkNeedsUpdate(): Boolean {
        lastAppVersion = context.appStateStore.data.map { it.lastAppVersion }.first()
        context.appStateStore.updateData {
            it.copy(lastAppVersion = BuildConfig.VERSION_CODE)
        }
        return lastAppVersion > 0 && lastAppVersion < BuildConfig.VERSION_CODE
    }

    /**
     * Update the app's working environment.
     * @return The [Result] of the update
     */
    fun doUpdate(): Result {
        return Result.NOT_NEEDED
    }
}
