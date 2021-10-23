package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.message.Message
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [CoroutineWorker] designed to validate app cache for the given watch.
 */
class AppCacheUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : BaseAppCacheUpdateWorker(context, workerParams), KoinComponent {

    private val watchManager: WatchManager by inject()

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
