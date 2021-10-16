package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.message.Message
import kotlinx.coroutines.flow.first

/**
 * A [CoroutineWorker] designed to validate app cache for the given watch.
 */
class AppCacheUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : BaseAppCacheUpdateWorker(context, workerParams) {

    private val watchManager by lazy { WatchManager.getInstance(applicationContext) }

    override suspend fun onSendCacheState(targetUid: String, cacheHash: Int): Boolean {
        val watch = watchManager.getWatchById(targetUid).first() ?: return false
        return watchManager.sendMessage(
            watch,
            Message(
                VALIDATE_CACHE,
                cacheHash
            )
        )
    }
}
