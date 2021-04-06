@file:Suppress("DEPRECATION")

package com.boswelja.devicemanager.bootorupdate.updater

import android.content.Context
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.appStateStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Updater(private val context: Context) {

    private var lastAppVersion: Int = BuildConfig.VERSION_CODE

    suspend fun checkNeedsUpdate(): Boolean {
        lastAppVersion = context.appStateStore.data.map { it.lastAppVersion }.first()
        return lastAppVersion > 0 && lastAppVersion < BuildConfig.VERSION_CODE
    }

    /**
     * Update the app's working environment.
     * @return The [Result] of the update
     */
    suspend fun doUpdate(): Result {
        var updateStatus = Result.NOT_NEEDED
        if (lastAppVersion < 2027000000) {
            updateStatus = Result.RECOMMEND_RESET
        }
        context.appStateStore.updateData {
            it.copy(lastAppVersion = BuildConfig.VERSION_CODE)
        }
        return updateStatus
    }
}
