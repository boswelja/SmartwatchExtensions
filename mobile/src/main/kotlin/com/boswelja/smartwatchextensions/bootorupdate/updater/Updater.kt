@file:Suppress("DEPRECATION")

package com.boswelja.smartwatchextensions.bootorupdate.updater

import android.content.Context
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.appStateStore
import com.boswelja.smartwatchextensions.appmanager.AppCacheUpdateWorker
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take

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
    suspend fun doUpdate(): Result {
        if (lastAppVersion <= 401011) {
            WatchDatabase.getInstance(context)
                .watchDao().getAll().take(1)
                .collect {
                    it.forEach { watch ->
                        AppCacheUpdateWorker.enqueueWorkerFor(context, watch.id)
                    }
                }
            return Result.COMPLETED
        }
        return Result.NOT_NEEDED
    }
}
